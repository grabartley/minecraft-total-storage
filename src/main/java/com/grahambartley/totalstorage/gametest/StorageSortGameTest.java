package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.config.SortOrdering;
import com.grahambartley.totalstorage.sort.SortComparator;
import com.grahambartley.totalstorage.sort.StorageSort;
import com.grahambartley.totalstorage.sort.StorageSortOutcome;
import com.grahambartley.totalstorage.sort.StorageSortResult;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import com.grahambartley.totalstorage.transaction.NetworkTransactions;
import com.grahambartley.totalstorage.transaction.OperationResult;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class StorageSortGameTest implements FabricGameTest {
  private static final String BATCH = "storage-sort";

  private static final int TICK_LIMIT = 20;

  private static final BlockPos CONTAINER = new BlockPos(3, 1, 3);

  private final StorageSort sorting = new StorageSort();
  private final StorageProviderResolver providers = new StorageProviderResolver();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void sortingABarrelByPositionCompactsIt(TestContext context) {
    Inventory barrel = placed(context, Blocks.BARREL);
    barrel.setStack(2, new ItemStack(Items.COBBLESTONE, 20));
    barrel.setStack(7, new ItemStack(Items.COBBLESTONE, 30));

    StorageSortResult result = sort(context, ContainerRole.GENERAL);

    context.assertTrue(
        result.outcome() == StorageSortOutcome.SORTED, "A barrel should sort, got " + result);
    context.assertEquals(barrel.getStack(0).getCount(), 50, "Both part stacks should merge");
    context.assertEquals(result.stacksMerged(), 1, "Two stacks became one");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFurnaceIsRefusedBecauseItsSlotsRestrictWhatTheyTake(TestContext context) {
    Inventory furnace = placed(context, Blocks.FURNACE);
    furnace.setStack(0, new ItemStack(Items.COBBLESTONE, 8));
    furnace.setStack(1, new ItemStack(Items.COAL, 4));

    StorageSortResult result = sort(context, ContainerRole.GENERAL);

    context.assertTrue(
        result.outcome() == StorageSortOutcome.NOT_SORTABLE,
        "A furnace's slots restrict what they take, got " + result);
    context.assertTrue(
        furnace.getStack(0).isOf(Items.COBBLESTONE) && furnace.getStack(0).getCount() == 8,
        "The input slot must be left where it was, found " + furnace.getStack(0));
    context.assertTrue(furnace.getStack(1).isOf(Items.COAL), "The fuel must stay in the fuel slot");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aBrewingStandIsRefusedForTheSameReason(TestContext context) {
    Inventory stand = placed(context, Blocks.BREWING_STAND);
    stand.setStack(3, new ItemStack(Items.NETHER_WART, 2));

    StorageSortResult result = sort(context, ContainerRole.GENERAL);

    context.assertTrue(
        result.outcome() == StorageSortOutcome.NOT_SORTABLE,
        "A brewing stand's slots restrict what they take, got " + result);
    context.assertTrue(
        stand.getStack(3).isOf(Items.NETHER_WART), "The ingredient must stay where it was");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsRefusedBeforeItsSlotsAreEvenRead(TestContext context) {
    Inventory barrel = placed(context, Blocks.BARREL);
    barrel.setStack(2, new ItemStack(Items.COBBLESTONE, 20));
    barrel.setStack(7, new ItemStack(Items.COBBLESTONE, 30));

    StorageSortResult result = sort(context, ContainerRole.EXCLUDED);

    context.assertTrue(
        result.outcome() == StorageSortOutcome.EXCLUDED,
        "An excluded container should be refused, got " + result);
    context.assertEquals(barrel.getStack(2).getCount(), 20, "Nothing should have moved");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEmptyContainerReportsThereWasNothingToSort(TestContext context) {
    placed(context, Blocks.BARREL);

    StorageSortResult result = sort(context, ContainerRole.GENERAL);

    context.assertTrue(
        result.outcome() == StorageSortOutcome.EMPTY,
        "An empty container has nothing to sort, got " + result);
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSortedContainerIsRecognisedRatherThanRearrangedAgain(TestContext context) {
    Inventory barrel = placed(context, Blocks.BARREL);
    barrel.setStack(2, new ItemStack(Items.COBBLESTONE, 20));
    barrel.setStack(7, new ItemStack(Items.COBBLESTONE, 30));
    sort(context, ContainerRole.GENERAL);

    StorageSortResult second = sort(context, ContainerRole.GENERAL);

    context.assertTrue(
        second.outcome() == StorageSortOutcome.ALREADY_SORTED,
        "A sorted container should be left alone, got " + second);
    context.assertEquals(barrel.getStack(0).getCount(), 50, "The arrangement should be unchanged");
    context.complete();
  }

  private StorageSortResult sort(TestContext context, ContainerRole role) {
    final StorageProvider provider =
        providers
            .resolve(context.getWorld(), context.getAbsolutePos(CONTAINER), role)
            .orElseThrow(() -> new IllegalStateException("no storage to sort"));
    final OperationResult<StorageSortResult> result =
        NetworkTransactions.runner()
            .run(
                transaction ->
                    OperationResult.committed(
                        sorting.sort(
                            provider,
                            SortComparator.forOrdering(SortOrdering.REGISTRY_ID),
                            transaction)));
    return result.orElse(StorageSortResult.of(StorageSortOutcome.FAILED));
  }

  private static Inventory placed(TestContext context, Block block) {
    context.setBlockState(CONTAINER, block.getDefaultState());
    return context.getBlockEntity(CONTAINER);
  }
}
