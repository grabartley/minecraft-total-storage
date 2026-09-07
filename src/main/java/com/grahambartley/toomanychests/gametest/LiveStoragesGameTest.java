package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.operation.LiveStorages;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Looking up the containers a network remembers, in a world where they may no longer be there.
 *
 * <p>Per ADR 0009 a recorded origin is a hint, and this is the class that decides what a stale one
 * costs. What matters is that a container which has gone drops out silently rather than resolving
 * to something empty: an operation handed a phantom storage would report a shortfall it could not
 * explain, and one handed an exception would fail a click that should simply have found less.
 *
 * <p>Only a world can show this, because a resolution is a block lookup.
 */
public final class LiveStoragesGameTest implements FabricGameTest {
  private static final String BATCH = "live-storages";
  private static final int TICK_LIMIT = 40;

  private final LiveStorages live = new LiveStorages();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyOriginStillStandingResolvesToItsContainer(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.EMERALD, 64);

    List<Storage<ItemVariant>> resolved =
        live.of(
            context.getWorld(),
            List.of(
                originAt(context, NetworkOperationFixture.EAST_BARREL),
                originAt(context, NetworkOperationFixture.WEST_BARREL)));

    context.assertTrue(resolved.size() == 2, "Resolved " + resolved.size() + " of two containers");
    context.complete();
  }

  /** The duplication-safety case: a container that has gone must not resolve to anything. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOriginWhoseContainerHasGoneDropsOutRatherThanResolvingEmpty(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.EMERALD, 64);
    StorageOrigin broken = originAt(context, NetworkOperationFixture.EAST_BARREL);
    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.AIR);

    List<Storage<ItemVariant>> resolved =
        live.of(
            context.getWorld(),
            List.of(broken, originAt(context, NetworkOperationFixture.WEST_BARREL)));

    context.assertTrue(
        resolved.size() == 1, "A broken container resolved, giving " + resolved.size());
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOriginPointingAtSomethingThatWasNeverAContainerResolvesToNothing(
      TestContext context) {
    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.STONE);

    List<Storage<ItemVariant>> resolved =
        live.of(
            context.getWorld(), List.of(originAt(context, NetworkOperationFixture.EAST_BARREL)));

    context.assertTrue(resolved.isEmpty(), "A stone block resolved as a container");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void nothingToLookUpResolvesToNothingRatherThanThrowing(TestContext context) {
    context.assertTrue(live.of(context.getWorld(), List.of()).isEmpty(), "Empty means empty");
    context.assertTrue(live.of(context.getWorld(), null).isEmpty(), "Nothing means empty");
    context.complete();
  }

  private static StorageOrigin originAt(TestContext context, BlockPos relative) {
    return BlockStorageOrigin.of(context.getWorld(), context.getAbsolutePos(relative));
  }
}
