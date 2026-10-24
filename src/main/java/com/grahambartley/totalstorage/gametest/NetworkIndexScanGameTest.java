package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import com.grahambartley.totalstorage.index.NetworkIndexScan;
import com.grahambartley.totalstorage.storage.ContainerDiscoveryScan;
import com.grahambartley.totalstorage.storage.ScanRegion;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class NetworkIndexScanGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "network-index";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CENTRE = new BlockPos(3, 1, 3);
  private static final int RADIUS = 1;
  private static final BlockPos NEAR = new BlockPos(2, 1, 2);
  private static final BlockPos FAR = new BlockPos(4, 1, 4);

  private final ContainerDiscoveryScan discovery = new ContainerDiscoveryScan();
  private final NetworkIndexScan indexScan = new NetworkIndexScan();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void identicalStacksInSeparateContainersAggregateIntoOneEntry(TestContext context) {
    stockBarrel(context, NEAR, new ItemStack(Items.COBBLESTONE, 64));
    stockBarrel(context, FAR, new ItemStack(Items.COBBLESTONE, 32));

    NetworkIndex<ItemVariant> index = indexHere(context);

    context.assertTrue(
        index.variantCount() == 1,
        "Identical stacks should be one entry, found " + index.variantCount());
    NetworkIndexEntry<ItemVariant> entry = index.entries().get(0);
    context.assertTrue(entry.totalCount() == 96L, "Counts should sum, found " + entry.totalCount());
    context.assertTrue(
        entry.providerCount() == 2,
        "Both containers should be recorded, found " + entry.providerCount());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void stacksDifferingOnlyByComponentsStaySeparateEntries(TestContext context) {
    ItemStack named = new ItemStack(Items.DIAMOND_SWORD);
    named.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sting"));
    stockBarrel(context, NEAR, new ItemStack(Items.DIAMOND_SWORD), named);

    NetworkIndex<ItemVariant> index = indexHere(context);

    context.assertTrue(
        index.variantCount() == 2,
        "A renamed sword should not merge with a plain one, found " + index.variantCount());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDoubleChestContributesOneOriginRatherThanTwo(TestContext context) {
    BlockPos leftHalf = new BlockPos(2, 1, 2);
    BlockPos rightHalf = new BlockPos(3, 1, 2);
    context.setBlockState(leftHalf, chestHalf(ChestType.LEFT));
    context.setBlockState(rightHalf, chestHalf(ChestType.RIGHT));
    ChestBlockEntity left = context.getBlockEntity(leftHalf);
    ChestBlockEntity right = context.getBlockEntity(rightHalf);
    left.setStack(0, new ItemStack(Items.COBBLESTONE, 64));
    right.setStack(0, new ItemStack(Items.COBBLESTONE, 64));

    NetworkIndex<ItemVariant> index = indexHere(context);

    NetworkIndexEntry<ItemVariant> entry = index.entries().get(0);
    context.assertTrue(
        entry.totalCount() == 128L, "Both halves should be counted, found " + entry.totalCount());
    context.assertTrue(
        entry.providerCount() == 1, "A double chest is one origin, found " + entry.providerCount());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void entriesAreOrderedByRegistryIdentifier(TestContext context) {
    stockBarrel(
        context,
        NEAR,
        new ItemStack(Items.TORCH, 5),
        new ItemStack(Items.DIAMOND, 12),
        new ItemStack(Items.COBBLESTONE, 64));

    NetworkIndex<ItemVariant> index = indexHere(context);

    List<Item> ordered = index.entries().stream().map(entry -> entry.variant().getItem()).toList();
    context.assertTrue(
        ordered.equals(List.of(Items.COBBLESTONE, Items.DIAMOND, Items.TORCH)),
        "Expected registry identifier order, found " + identifiers(ordered));
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void emptyContainersProduceAnEmptyIndex(TestContext context) {
    context.setBlockState(NEAR, Blocks.BARREL.getDefaultState());

    NetworkIndex<ItemVariant> index = indexHere(context);

    context.assertTrue(
        index.isEmpty(), "An empty barrel should index nothing, found " + index.variantCount());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void containersOutsideTheRadiusAreNotIndexed(TestContext context) {
    stockBarrel(context, NEAR, new ItemStack(Items.COBBLESTONE, 64));
    stockBarrel(context, new BlockPos(6, 1, 6), new ItemStack(Items.DIAMOND, 64));

    NetworkIndex<ItemVariant> index = indexHere(context);

    context.assertTrue(
        index.countOf(ItemVariant.of(Items.DIAMOND)) == 0L,
        "An out of range barrel should not reach the index");
    context.assertTrue(
        index.countOf(ItemVariant.of(Items.COBBLESTONE)) == 64L,
        "The in range barrel should still be indexed");
    context.complete();
  }

  private NetworkIndex<ItemVariant> indexHere(TestContext context) {
    return indexScan.index(
        discovery.scan(context.getWorld(), new ScanRegion(context.getAbsolutePos(CENTRE), RADIUS)));
  }

  private static void stockBarrel(TestContext context, BlockPos pos, ItemStack... stacks) {
    context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(pos);
    for (int slot = 0; slot < stacks.length; slot++) {
      barrel.setStack(slot, stacks[slot]);
    }
  }

  private static List<String> identifiers(List<Item> items) {
    return items.stream().map(item -> Registries.ITEM.getId(item).toString()).toList();
  }

  private static BlockState chestHalf(ChestType type) {
    return Blocks.CHEST
        .getDefaultState()
        .with(ChestBlock.FACING, Direction.NORTH)
        .with(ChestBlock.CHEST_TYPE, type);
  }
}
