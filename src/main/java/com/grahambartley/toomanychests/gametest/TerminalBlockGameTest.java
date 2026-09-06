package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.ModItems;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

/**
 * What the terminal block only does inside a running world: what placing it produces, what breaking
 * it costs and leaves behind, and that its settings outlive both a power change and a chunk load.
 *
 * <p>The settings themselves are proven in JUnit. What is proven here is the plumbing around them:
 * that the block entity is created at all, that it reaches NBT, and that vanilla's own chunk-load
 * deserialization brings back what was configured.
 */
public final class TerminalBlockGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-block";

  private static final BlockPos FLOOR = new BlockPos(3, 0, 3);
  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);
  private static final UUID OWNING_PLAYER = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID TRUSTED_PLAYER =
      UUID.fromString("00000000-0000-0000-0000-0000000000b2");

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void placingTheTerminalPointsItsScreenAtThePlayer(TestContext context) {
    ServerPlayerEntity player = placingPlayerFacing(context, Direction.SOUTH);

    context.useStackOnBlock(player, new ItemStack(ModItems.TERMINAL), FLOOR, Direction.UP);

    context.expectBlock(ModBlocks.TERMINAL, TERMINAL);
    context.assertTrue(
        context.getBlockState(TERMINAL).get(HorizontalFacingBlock.FACING) == Direction.NORTH,
        "A terminal placed by a player looking south should face back north at them");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aFreshlyPlacedTerminalIsUnpowered(TestContext context) {
    ServerPlayerEntity player = placingPlayerFacing(context, Direction.SOUTH);

    context.useStackOnBlock(player, new ItemStack(ModItems.TERMINAL), FLOOR, Direction.UP);

    context.assertFalse(
        context.getBlockState(TERMINAL).get(TerminalBlock.POWERED),
        "A terminal with no redstone reaching it should place unpowered");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void placingTheTerminalRecordsThePlacingPlayerAsItsOwner(TestContext context) {
    ServerPlayerEntity player = placingPlayerFacing(context, Direction.SOUTH);

    context.useStackOnBlock(player, new ItemStack(ModItems.TERMINAL), FLOOR, Direction.UP);

    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    context.assertTrue(
        terminal.settings().isOwnedBy(player.getUuid()),
        "A placed terminal should belong to whoever placed it");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void theTerminalNeedsAPickaxeToBeHarvested(TestContext context) {
    placeTerminal(context);
    BlockState state = context.getBlockState(TERMINAL);
    PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);

    player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
    context.assertFalse(player.canHarvest(state), "A bare hand should not harvest the terminal");

    player
        .getInventory()
        .setStack(player.getInventory().selectedSlot, new ItemStack(Items.STONE_PICKAXE));
    context.assertTrue(player.canHarvest(state), "A pickaxe should harvest the terminal");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingTheTerminalDropsItBack(TestContext context) {
    placeTerminal(context);

    context.getWorld().breakBlock(context.getAbsolutePos(TERMINAL), true);

    int dropped = droppedTerminals(context);
    context.assertTrue(
        dropped == 1,
        "Breaking a terminal should drop exactly one terminal back, dropped " + dropped);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingTheTerminalLeavesAConnectedContainerAlone(TestContext context) {
    placeTerminal(context);
    context.setBlockState(BARREL, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    barrel.setStack(0, new ItemStack(Items.DIAMOND, 8));

    context.getWorld().breakBlock(context.getAbsolutePos(TERMINAL), true);

    BarrelBlockEntity survivor = context.getBlockEntity(BARREL);
    context.assertTrue(
        survivor.getStack(0).isOf(Items.DIAMOND) && survivor.getStack(0).getCount() == 8,
        "A container the terminal could see should be untouched by the terminal going");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void changingThePowerStateKeepsTheSettings(TestContext context) {
    placeTerminal(context);
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(11).withTag("ores"));

    context.setBlockState(
        TERMINAL, context.getBlockState(TERMINAL).with(TerminalBlock.POWERED, true));
    context.setBlockState(
        TERMINAL, context.getBlockState(TERMINAL).with(TerminalBlock.POWERED, false));

    TerminalBlockEntity afterPowerCut = context.getBlockEntity(TERMINAL);
    context.assertTrue(
        afterPowerCut.settings().radius() == 11 && afterPowerCut.settings().hasTag("ores"),
        "Cutting power is an off switch, not a factory reset");
    context.complete();
  }

  /**
   * A gametest cannot unload the chunk it is standing in, so the reload is driven through {@link
   * BlockEntity#createFromNbt}, the same call vanilla makes for every block entity in a chunk it
   * loads from disk.
   */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void everySettingSurvivesAWorldReload(TestContext context) {
    placeTerminal(context);
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(
        settings ->
            settings
                .withRadius(7)
                .withAccessMode(TerminalAccessMode.TRUSTED)
                .withOwner(OWNING_PLAYER)
                .withTrusted(TRUSTED_PLAYER)
                .withContainerRole(context.getAbsolutePos(BARREL), ContainerRole.EXCLUDED)
                .withTag("ores"));
    TerminalSettings configured = terminal.settings();

    RegistryWrapper.WrapperLookup lookup = context.getWorld().getRegistryManager();
    NbtCompound saved = terminal.createNbtWithIdentifyingData(lookup);
    BlockEntity reloaded =
        BlockEntity.createFromNbt(
            context.getAbsolutePos(TERMINAL), context.getBlockState(TERMINAL), saved, lookup);

    context.assertTrue(
        reloaded instanceof TerminalBlockEntity,
        "A saved terminal should load back as a terminal block entity");
    context.assertTrue(
        configured.equals(((TerminalBlockEntity) reloaded).settings()),
        "Reloaded settings should match what was configured, got "
            + ((TerminalBlockEntity) reloaded).settings());
    context.complete();
  }

  private void placeTerminal(TestContext context) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
  }

  private ServerPlayerEntity placingPlayerFacing(TestContext context, Direction facing) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(new BlockPos(3, 1, 5));
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, facing.asRotation(), 0.0f);
    return player;
  }

  private int droppedTerminals(TestContext context) {
    Box around = Box.from(context.getAbsolutePos(TERMINAL).toCenterPos()).expand(2.0);
    List<ItemEntity> drops =
        context
            .getWorld()
            .getEntitiesByClass(
                ItemEntity.class, around, drop -> drop.getStack().isOf(ModItems.TERMINAL));
    return drops.stream().mapToInt(drop -> drop.getStack().getCount()).sum();
  }
}
