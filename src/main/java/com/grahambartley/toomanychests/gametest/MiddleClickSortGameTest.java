package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigBuilder;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.sort.ContainerSortService;
import com.grahambartley.toomanychests.sort.SortOutcome;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class MiddleClickSortGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "middle-click-sort";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CONTAINER = new BlockPos(3, 1, 3);
  private static final int MIDDLE_BUTTON = 2;
  private static final int FIRST_SLOT = 0;

  private final ContainerSortService sorting = new ContainerSortService();

  private TooManyChestsConfig original = TooManyChestsConfig.defaults();

  @BeforeBatch(batchId = BATCH)
  public void rememberTheConfigAndClearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
    original = ServerConfigService.current();
  }

  @AfterBatch(batchId = BATCH)
  public void restoreTheConfig(ServerWorld world) {
    ServerConfigService.update(world.getServer(), original);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aSurvivalMiddleClickSortsTheChestTheVanillaClientHasOpen(TestContext context) {
    final ServerPlayerEntity player = openChest(context, GameMode.SURVIVAL);
    final Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));

    middleClick(player);

    context.assertEquals(chest.getStack(0).getCount(), 50, "The part stacks should have merged");
    context.assertTrue(chest.getStack(5).isEmpty(), "The slot behind it should be freed");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aCreativeMiddleClickStillClonesTheStackItIsOver(TestContext context) {
    final ServerPlayerEntity player = openChest(context, GameMode.CREATIVE);
    final Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));

    middleClick(player);

    context.assertTrue(
        player.currentScreenHandler.getCursorStack().isOf(Items.COBBLESTONE),
        "Creative middle click should have cloned the stack onto the cursor");
    context.assertEquals(
        chest.getStack(5).getCount(), 30, "Creative middle click must not sort the chest");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aServerThatSwitchedTheGestureOffLeavesTheChestAlone(TestContext context) {
    ServerConfigService.update(
        context.getWorld().getServer(), withGesture(ServerConfigService.current(), false));
    final ServerPlayerEntity player = openChest(context, GameMode.SURVIVAL);
    final Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));

    middleClick(player);

    final int leftAlone = chest.getStack(5).getCount();
    ServerConfigService.update(context.getWorld().getServer(), original);
    context.assertEquals(leftAlone, 30, "A switched off gesture must move nothing");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aServerThatSwitchedTheGestureOffLeavesTheSortCommandWorking(TestContext context) {
    ServerConfigService.update(
        context.getWorld().getServer(), withGesture(ServerConfigService.current(), false));
    final ServerPlayerEntity player = openChest(context, GameMode.SURVIVAL);
    final Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));

    final SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    ServerConfigService.update(context.getWorld().getServer(), original);
    context.assertTrue(
        outcome.wasSorted(), "The command path should still sort, but reported " + outcome);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aMiddleClickSortOfTheInventoryLeavesALockedSlotWhereItIs(TestContext context) {
    final ServerPlayerEntity player = survivalPlayer(context);
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(0)));
    final Inventory inventory = player.getInventory();
    inventory.setStack(0, new ItemStack(Items.DIAMOND, 1));
    inventory.setStack(4, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(9, new ItemStack(Items.COBBLESTONE, 30));

    middleClick(player);

    context.assertTrue(
        inventory.getStack(0).isOf(Items.DIAMOND),
        "The locked slot must be untouched, found " + inventory.getStack(0));
    context.assertEquals(inventory.getStack(1).getCount(), 50, "The rest should merge around it");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPlayersOwnShiftClickStillMovesWhatALockedSlotHolds(TestContext context) {
    final ServerPlayerEntity player = openChest(context, GameMode.SURVIVAL);
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(0)));
    final ScreenHandler handler = player.currentScreenHandler;
    final int lockedSlot = handler.slots.size() - LockedSlots.SLOT_COUNT;
    handler.getSlot(lockedSlot).setStack(new ItemStack(Items.DIAMOND, 1));

    handler.onSlotClick(lockedSlot, FIRST_SLOT, SlotActionType.QUICK_MOVE, player);

    context.assertTrue(
        handler.getSlot(lockedSlot).getStack().isEmpty(),
        "A lock must never fight the player's own hands");
    finish(context, player);
  }

  private static TooManyChestsConfig withGesture(TooManyChestsConfig config, boolean enabled) {
    return ConfigBuilder.from(config).middleClickSortEnabled(enabled).build();
  }

  private static void middleClick(ServerPlayerEntity player) {
    player.currentScreenHandler.onSlotClick(
        FIRST_SLOT, MIDDLE_BUTTON, SlotActionType.CLONE, player);
  }

  private static ServerPlayerEntity survivalPlayer(TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    player.getInventory().clear();
    return player;
  }

  private static ServerPlayerEntity openChest(TestContext context, GameMode mode) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(mode);
    player.getInventory().clear();
    context.setBlockState(CONTAINER, Blocks.CHEST.getDefaultState());
    final NamedScreenHandlerFactory factory =
        context
            .getBlockState(CONTAINER)
            .createScreenHandlerFactory(context.getWorld(), context.getAbsolutePos(CONTAINER));
    player.openHandledScreen(factory);
    return player;
  }

  private static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
      player.closeHandledScreen();
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    context.complete();
  }
}
