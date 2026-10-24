package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.config.ConfigBuilder;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.lock.LockedSlots;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import com.grahambartley.totalstorage.sort.ContainerSortService;
import com.grahambartley.totalstorage.sort.SortOutcome;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerSortServiceGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "container-sort";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CONTAINER = new BlockPos(3, 1, 3);
  private static final BlockPos OTHER_CONTAINER = new BlockPos(1, 1, 1);
  private static final int UNOPENED_SYNC_ID = 77;
  private static final int FIRST_MAIN_SLOT = PlayerInventory.getHotbarSize();

  private final ContainerSortService sorting = new ContainerSortService();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAChestGathersItsPartStacksTogether(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));
    chest.setStack(9, new ItemStack(Items.COBBLESTONE, 14));

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(outcome.wasSorted(), "A chest should sort but reported " + outcome);
    context.assertEquals(chest.getStack(0).getCount(), 64, "Three part stacks make one full stack");
    context.assertTrue(chest.getStack(5).isEmpty(), "The slots behind it should be freed");
    context.assertTrue(chest.getStack(9).isEmpty(), "The slots behind it should be freed");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAChestOrdersWhatItLeavesBehind(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.DIAMOND, 1));
    chest.setStack(1, new ItemStack(Items.OAK_LOG, 1));

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        chest.getStack(0).isOf(Items.OAK_LOG),
        "Wood sorts ahead of ores, found " + chest.getStack(0).getItem());
    context.assertTrue(chest.getStack(1).isOf(Items.DIAMOND), "The ore should follow the wood");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingABarrelWorksTheSameAsAChest(TestContext context) {
    assertGathersCobblestone(context, Blocks.BARREL);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAPlacedShulkerBoxWorksTheSameAsAChest(TestContext context) {
    assertGathersCobblestone(context, Blocks.SHULKER_BOX);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingThePlayerInventoryGathersItsPartStacks(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    Inventory inventory = player.getInventory();
    inventory.setStack(12, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(20, new ItemStack(Items.COBBLESTONE, 30));

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(outcome.wasSorted(), "An inventory should sort but reported " + outcome);
    context.assertEquals(
        inventory.getStack(FIRST_MAIN_SLOT).getCount(), 50, "Both part stacks should merge");
    context.assertEquals(totalIn(inventory), 50, "Nothing should be lost or invented");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingThePlayerInventoryLeavesEveryHotbarSlotAsItWas(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    Inventory inventory = player.getInventory();
    inventory.setStack(0, new ItemStack(Items.DIORITE, 5));
    inventory.setStack(3, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(8, new ItemStack(Items.IRON_PICKAXE, 1));
    inventory.setStack(15, new ItemStack(Items.COBBLESTONE, 30));
    inventory.setStack(30, new ItemStack(Items.OAK_LOG, 4));
    List<ItemStack> hotbarBefore = snapshotOf(inventory).subList(0, FIRST_MAIN_SLOT);

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(outcome.wasSorted(), "The main inventory should sort, reported " + outcome);
    context.assertTrue(
        matches(hotbarBefore, snapshotOf(inventory).subList(0, FIRST_MAIN_SLOT)),
        "Every hotbar slot must hold exactly what it held before");
    context.assertTrue(
        inventory.getStack(FIRST_MAIN_SLOT).isOf(Items.COBBLESTONE)
            && inventory.getStack(FIRST_MAIN_SLOT).getCount() == 30,
        "The main inventory sorts without pulling from the hotbar, found "
            + inventory.getStack(FIRST_MAIN_SLOT));
    context.assertEquals(totalIn(inventory), 60, "Nothing should be lost or invented");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRequestNamingAScreenThePlayerDoesNotHaveOpenIsRefused(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 20));

    SortOutcome outcome = sorting.sort(player, UNOPENED_SYNC_ID);

    context.assertTrue(outcome.wasRefused(), "A stale sync id should be refused, got " + outcome);
    context.assertEquals(
        chest.getStack(5).getCount(), 20, "A refused request must not move anything");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRequestNamingAnotherPlayersChestReachesNothing(TestContext context) {
    ServerPlayerEntity opener = openContainer(context, Blocks.CHEST);
    ServerPlayerEntity bystander = context.createMockCreativeServerPlayerInWorld();
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 20));

    SortOutcome outcome = sorting.sort(bystander, opener.currentScreenHandler.syncId);

    context.assertTrue(
        !outcome.wasSorted(), "Someone else's sync id should reach nothing, got " + outcome);
    context.assertEquals(
        chest.getStack(5).getCount(), 20, "The chest must be exactly as it was left");
    finish(context, opener, bystander);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aCraftingTableRefusesSorting(TestContext context) {
    assertRefuses(context, Blocks.CRAFTING_TABLE);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFurnaceRefusesSorting(TestContext context) {
    assertRefuses(context, Blocks.FURNACE);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anAnvilRefusesSorting(TestContext context) {
    assertRefuses(context, Blocks.ANVIL);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aLockedInventorySlotKeepsItsPositionAndItsContents(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(FIRST_MAIN_SLOT)));
    Inventory inventory = player.getInventory();
    inventory.setStack(FIRST_MAIN_SLOT, new ItemStack(Items.DIAMOND, 1));
    inventory.setStack(14, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(20, new ItemStack(Items.COBBLESTONE, 30));

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(outcome.wasSorted(), "The rest should still sort, reported " + outcome);
    context.assertTrue(
        inventory.getStack(FIRST_MAIN_SLOT).isOf(Items.DIAMOND)
            && inventory.getStack(FIRST_MAIN_SLOT).getCount() == 1,
        "The locked slot must be untouched, found " + inventory.getStack(FIRST_MAIN_SLOT));
    context.assertEquals(
        inventory.getStack(FIRST_MAIN_SLOT + 1).getCount(), 50, "The rest should merge around it");
    context.assertEquals(totalIn(inventory), 51, "Nothing should be lost or invented");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyLockedSlotSurvivesASortOfAFullInventory(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(12, 17, 31)));
    Inventory inventory = player.getInventory();
    for (int slot = 0; slot < LockedSlots.SLOT_COUNT; slot++) {
      inventory.setStack(slot, new ItemStack(Items.COBBLESTONE, slot + 1));
    }
    List<ItemStack> lockedBefore =
        List.of(
            inventory.getStack(12).copy(),
            inventory.getStack(17).copy(),
            inventory.getStack(31).copy());
    int before = totalIn(inventory);

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        ItemStack.areEqual(inventory.getStack(12), lockedBefore.get(0))
            && ItemStack.areEqual(inventory.getStack(17), lockedBefore.get(1))
            && ItemStack.areEqual(inventory.getStack(31), lockedBefore.get(2)),
        "Every locked slot must hold exactly what it held before");
    context.assertEquals(totalIn(inventory), before, "Sorting must conserve every item");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAFullChestOfMixedItemsConservesEveryOne(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    List<Item> items =
        List.of(Items.COBBLESTONE, Items.OAK_LOG, Items.DIAMOND, Items.APPLE, Items.REDSTONE);
    for (int slot = 0; slot < chest.size(); slot++) {
      chest.setStack(slot, new ItemStack(items.get(slot % items.size()), (slot % 7) + 1));
    }
    int before = totalIn(chest);

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(outcome.wasSorted(), "A full chest should sort but reported " + outcome);
    context.assertEquals(totalIn(chest), before, "Sorting must conserve every item");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAnAlreadySortedChestLeavesItExactlyAsItWas(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.OAK_LOG, 12));
    chest.setStack(1, new ItemStack(Items.DIAMOND, 3));
    sorting.sort(player, player.currentScreenHandler.syncId);
    List<ItemStack> once = snapshotOf(chest);

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        matches(once, snapshotOf(chest)), "Sorting twice should leave the same arrangement");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEmptyChestReportsThereWasNothingToSort(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        outcome == SortOutcome.NOTHING_TO_SORT,
        "An empty chest has nothing to sort, reported " + outcome);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStacksizeLimitedItemIsSplitAcrossSlotsRatherThanOverfilled(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.ENDER_PEARL, 10));
    chest.setStack(4, new ItemStack(Items.ENDER_PEARL, 10));

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertEquals(chest.getStack(0).getCount(), 16, "Ender pearls stack to sixteen");
    context.assertEquals(chest.getStack(1).getCount(), 4, "The remainder goes in the next slot");
    context.assertEquals(totalIn(chest), 20, "Nothing should be lost or invented");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aSortNeverReachesTheContainerNextToTheOpenOne(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    context.setBlockState(OTHER_CONTAINER, Blocks.BARREL.getDefaultState());
    Inventory neighbour = context.getBlockEntity(OTHER_CONTAINER);
    neighbour.setStack(5, new ItemStack(Items.COBBLESTONE, 20));
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 1));

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertEquals(
        neighbour.getStack(5).getCount(), 20, "A container nobody opened must not move");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingAChestNeverTouchesThePlayersOwnInventory(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory inventory = player.getInventory();
    inventory.setStack(8, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(2, new ItemStack(Items.COBBLESTONE, 30));
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(9, new ItemStack(Items.DIAMOND, 1));

    sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertEquals(
        inventory.getStack(8).getCount(), 20, "The player's slots must be left where they were");
    context.assertEquals(
        inventory.getStack(2).getCount(), 30, "The player's slots must be left where they were");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRequestFromNobodyIsRefused(TestContext context) {
    context.assertTrue(
        sorting.sort(null, 0).wasRefused(), "A request with no player should be refused");
    context.complete();
  }

  private void assertGathersCobblestone(TestContext context, Block block) {
    ServerPlayerEntity player = openContainer(context, block);
    Inventory container = context.getBlockEntity(CONTAINER);
    container.setStack(2, new ItemStack(Items.COBBLESTONE, 20));
    container.setStack(7, new ItemStack(Items.COBBLESTONE, 30));

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        outcome.wasSorted(), block.getName().getString() + " should sort but reported " + outcome);
    context.assertEquals(container.getStack(0).getCount(), 50, "Both part stacks should merge");
    context.assertEquals(totalIn(container), 50, "Nothing should be lost or invented");
    finish(context, player);
  }

  private void assertRefuses(TestContext context, Block block) {
    ServerPlayerEntity player = openContainer(context, block);

    SortOutcome outcome = sorting.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        outcome == SortOutcome.SCREEN_NOT_SORTABLE,
        block.getName().getString() + " must refuse sorting, reported " + outcome);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aServerThatSwitchedOffTheButtonRefusesWhatTheButtonSends(TestContext context) {
    ServerPlayerEntity player = openContainer(context, Blocks.CHEST);
    Inventory chest = context.getBlockEntity(CONTAINER);
    chest.setStack(0, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(5, new ItemStack(Items.COBBLESTONE, 30));
    TotalStorageConfig original = ServerConfigService.current();
    ServerConfigService.update(context.getWorld().getServer(), withSortButton(original, false));

    SortOutcome refused = sorting.sortFromButton(player, player.currentScreenHandler.syncId);
    int leftAlone = chest.getStack(5).getCount();
    ServerConfigService.update(context.getWorld().getServer(), withSortButton(original, true));
    SortOutcome offered = sorting.sortFromButton(player, player.currentScreenHandler.syncId);
    ServerConfigService.update(context.getWorld().getServer(), original);

    context.assertTrue(
        refused == SortOutcome.NOT_OFFERED,
        "A switched off sort button should refuse rather than sort, but reported " + refused);
    context.assertEquals(leftAlone, 30, "Nothing should have moved while it was switched off");
    context.assertTrue(
        offered.wasSorted(), "Switching the button back on should sort but reported " + offered);
    finish(context, player);
  }

  private static TotalStorageConfig withSortButton(TotalStorageConfig config, boolean enabled) {
    return ConfigBuilder.from(config).sortButtonEnabled(enabled).build();
  }

  private static ServerPlayerEntity openContainer(TestContext context, Block block) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.setBlockState(CONTAINER, block.getDefaultState());
    BlockPos absolute = context.getAbsolutePos(CONTAINER);
    NamedScreenHandlerFactory factory =
        context.getBlockState(CONTAINER).createScreenHandlerFactory(context.getWorld(), absolute);
    if (factory == null) {
      throw new IllegalStateException("no screen for " + block.getName().getString());
    }
    player.openHandledScreen(factory);
    return player;
  }

  private static int totalIn(Inventory inventory) {
    int total = 0;
    for (int slot = 0; slot < inventory.size(); slot++) {
      total += inventory.getStack(slot).getCount();
    }
    return total;
  }

  private static List<ItemStack> snapshotOf(Inventory inventory) {
    List<ItemStack> stacks = new ArrayList<>(inventory.size());
    for (int slot = 0; slot < inventory.size(); slot++) {
      stacks.add(inventory.getStack(slot).copy());
    }
    return stacks;
  }

  private static boolean matches(List<ItemStack> before, List<ItemStack> after) {
    if (before.size() != after.size()) {
      return false;
    }
    for (int slot = 0; slot < before.size(); slot++) {
      if (!ItemStack.areEqual(before.get(slot), after.get(slot))) {
        return false;
      }
    }
    return true;
  }

  private static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.closeHandledScreen();
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    context.complete();
  }
}
