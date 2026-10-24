package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.lock.LockedSlots;
import com.grahambartley.totalstorage.sort.SortTarget;
import com.grahambartley.totalstorage.sort.SortableSlots;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class SortableSlotsGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "sortable-slots";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CONTAINER = new BlockPos(3, 1, 3);
  private static final int CHEST_SLOTS = 27;
  private static final int SHULKER_SLOTS = 27;
  private static final int MAIN_INVENTORY_SLOTS =
      LockedSlots.SLOT_COUNT - PlayerInventory.getHotbarSize();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aChestResolvesToItsOwnSlotsAndNoneOfThePlayers(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target = targetFor(context, player, Blocks.CHEST).orElseThrow();

    context.assertEquals(target.slotCount(), CHEST_SLOTS, "A chest offers its own slots only");
    context.assertTrue(
        target.inventory() != player.getInventory(),
        "Sorting a chest must never reach into the player's inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBarrelResolvesToItsOwnSlots(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target = targetFor(context, player, Blocks.BARREL).orElseThrow();

    context.assertEquals(target.slotCount(), CHEST_SLOTS, "A barrel offers twenty seven slots");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aShulkerBoxResolvesDespiteRefusingToHoldItself(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target = targetFor(context, player, Blocks.SHULKER_BOX).orElseThrow();

    context.assertEquals(target.slotCount(), SHULKER_SLOTS, "A shulker box offers its own slots");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theOpenPlayerInventoryResolvesToTheMainInventoryOnly(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target =
        SortableSlots.resolve(player.currentScreenHandler, player, LockedSlots.empty())
            .orElseThrow();

    context.assertEquals(
        target.slotCount(), MAIN_INVENTORY_SLOTS, "Only the main inventory is sortable");
    context.assertTrue(
        target.indices().stream().noneMatch(index -> index < PlayerInventory.getHotbarSize()),
        "No hotbar slot should be sortable, got " + target.indices());
    context.assertTrue(
        target.inventory() == player.getInventory(), "The target should be the player's inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void armourAndTheOffhandAreNeverSortable(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target =
        SortableSlots.resolve(player.currentScreenHandler, player, LockedSlots.empty())
            .orElseThrow();

    context.assertTrue(
        target.indices().stream().noneMatch(index -> index > LockedSlots.LAST_SLOT),
        "No index above the lockable range should be sortable, got " + target.indices());
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aLockedSlotIsLeftOutOfTheTargetEntirely(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    SortTarget target =
        SortableSlots.resolve(
                player.currentScreenHandler, player, LockedSlots.of(List.of(9, 20, 35)))
            .orElseThrow();

    context.assertEquals(
        target.slotCount(), MAIN_INVENTORY_SLOTS - 3, "Three locked slots leave twenty four");
    context.assertTrue(
        target.indices().stream().noneMatch(List.of(9, 20, 35)::contains),
        "A locked index must not appear in the target, got " + target.indices());
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aCraftingTableIsRefused(TestContext context) {
    assertRefused(context, Blocks.CRAFTING_TABLE);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFurnaceIsRefused(TestContext context) {
    assertRefused(context, Blocks.FURNACE);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anAnvilIsRefused(TestContext context) {
    assertRefused(context, Blocks.ANVIL);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBrewingStandIsRefused(TestContext context) {
    assertRefused(context, Blocks.BREWING_STAND);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEnchantingTableIsRefused(TestContext context) {
    assertRefused(context, Blocks.ENCHANTING_TABLE);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aScreenWithNothingOpenResolvesToTheInventoryRatherThanNothing(TestContext context) {
    ServerPlayerEntity player = playerAt(context);

    context.assertTrue(
        SortableSlots.resolve(null, player, LockedSlots.empty()).isEmpty(),
        "No screen means nothing to sort");
    context.assertTrue(
        SortableSlots.resolve(player.currentScreenHandler, null, LockedSlots.empty()).isEmpty(),
        "No player means nothing to sort");
    finish(context, player);
  }

  private static void assertRefused(TestContext context, Block block) {
    ServerPlayerEntity player = playerAt(context);

    Optional<SortTarget> target = targetFor(context, player, block);

    context.assertTrue(
        target.isEmpty(), block.getName().getString() + " must refuse sorting, resolved " + target);
    finish(context, player);
  }

  private static Optional<SortTarget> targetFor(
      TestContext context, ServerPlayerEntity player, Block block) {
    open(context, player, block);
    return SortableSlots.resolve(player.currentScreenHandler, player, LockedSlots.empty());
  }

  private static void open(TestContext context, ServerPlayerEntity player, Block block) {
    context.setBlockState(CONTAINER, block.getDefaultState());
    BlockPos absolute = context.getAbsolutePos(CONTAINER);
    NamedScreenHandlerFactory factory =
        context.getBlockState(CONTAINER).createScreenHandlerFactory(context.getWorld(), absolute);
    if (factory == null) {
      throw new IllegalStateException("no screen for " + block.getName().getString());
    }
    player.openHandledScreen(factory);
  }

  private static ServerPlayerEntity playerAt(TestContext context) {
    return context.createMockCreativeServerPlayerInWorld();
  }

  private static void finish(TestContext context, ServerPlayerEntity player) {
    player.closeHandledScreen();
    context.getWorld().getServer().getPlayerManager().remove(player);
    context.complete();
  }
}
