package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.index.NetworkIndexSession;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * The half of invalidation that unit tests cannot reach: that real containers actually announce
 * their changes. Aggregation and laziness are proven in JUnit, so what is proven here is that
 * vanilla's {@code markDirty} reaches the registry from hand placement and from a ticking hopper,
 * and that a powered network nobody touches does nothing at all.
 */
public final class NetworkIndexInvalidationGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "index-invalidation";

  private static final BlockPos CENTRE = new BlockPos(3, 1, 3);
  private static final int RADIUS = 1;
  private static final BlockPos WATCHED = new BlockPos(2, 1, 2);
  private static final BlockPos ALSO_WATCHED = new BlockPos(4, 1, 4);
  private static final BlockPos THIRD_WATCHED = new BlockPos(2, 1, 4);
  private static final BlockPos HOPPER_ABOVE_WATCHED = new BlockPos(2, 2, 2);
  private static final BlockPos OUT_OF_RANGE = new BlockPos(6, 1, 6);

  private final ContainerDiscoveryScan discovery = new ContainerDiscoveryScan();

  @BeforeBatch(batchId = BATCH)
  public void dropListenersLeftBehindByEarlierBatches(ServerWorld world) {
    ContainerMutations.registry().clear();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void placingItemsIntoAWatchedContainerUpdatesTheListing(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();

    BarrelBlockEntity barrel = context.getBlockEntity(WATCHED);
    barrel.setStack(1, new ItemStack(Items.DIAMOND, 5));

    long diamonds = session.index().countOf(ItemVariant.of(Items.DIAMOND));
    context.assertTrue(
        diamonds == 5L, "Hand placement should reach the listing, found " + diamonds);
    session.end();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void updatingTheListingReReadsOnlyTheContainerThatChanged(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.DIAMOND, 8));
    stockBarrel(context, THIRD_WATCHED, new ItemStack(Items.TORCH, 4));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();
    int readsAfterFirstBuild = session.containerReadCount();

    BarrelBlockEntity barrel = context.getBlockEntity(WATCHED);
    barrel.setStack(1, new ItemStack(Items.COBBLESTONE, 16));
    session.index();

    int reReads = session.containerReadCount() - readsAfterFirstBuild;
    context.assertTrue(readsAfterFirstBuild == 3, "The first build reads every container");
    context.assertTrue(
        reReads == 1, "Only the changed container should be re-read, re-read " + reReads);
    session.end();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 100)
  public void aHopperTricklingIntoAWatchedContainerRebuildsTheIndexOnceOnly(TestContext context) {
    stockBarrel(context, WATCHED);
    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();

    AtomicInteger mutations = countMutationsOf(context, WATCHED);
    context.setBlockState(HOPPER_ABOVE_WATCHED, Blocks.HOPPER.getDefaultState());
    HopperBlockEntity hopper = context.getBlockEntity(HOPPER_ABOVE_WATCHED);
    hopper.setStack(0, new ItemStack(Items.COBBLESTONE, 64));

    context.runAtTick(
        80,
        () -> {
          int announced = mutations.get();
          long stored = session.index().countOf(ItemVariant.of(Items.COBBLESTONE));
          context.assertTrue(
              announced > 1, "The hopper should announce many changes, announced " + announced);
          context.assertTrue(stored > 0L, "The hopper's items should reach the listing");
          context.assertTrue(
              session.recomputeCount() == 2,
              "Many changes then one read is one rebuild, rebuilt " + session.recomputeCount());
          session.end();
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 100)
  public void aPoweredNetworkNobodyTouchesDoesNoWorkAsTheTicksPass(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();

    context.runAtEveryTick(session::index);
    context.runAtTick(
        80,
        () -> {
          context.assertTrue(
              session.recomputeCount() == 1,
              "An untouched network should never rebuild, rebuilt " + session.recomputeCount());
          context.assertTrue(
              session.containerReadCount() == 2,
              "An untouched network should never re-read, read " + session.containerReadCount());
          session.end();
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aChangeOutsideTheNetworkNeverReachesTheIndex(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    stockBarrel(context, OUT_OF_RANGE, new ItemStack(Items.DIAMOND, 8));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();

    BarrelBlockEntity distant = context.getBlockEntity(OUT_OF_RANGE);
    distant.setStack(1, new ItemStack(Items.DIAMOND, 8));
    session.index();

    context.assertTrue(
        session.recomputeCount() == 1,
        "A container out of range should not invalidate anything, rebuilt "
            + session.recomputeCount());
    session.end();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void unpoweringTheTerminalRemovesEveryListenerItRegistered(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();
    context.assertTrue(
        ContainerMutations.registry().isWatched(originOf(context, WATCHED)),
        "A powered terminal should be listening");

    session.end();

    context.assertTrue(
        !ContainerMutations.registry().isWatched(originOf(context, WATCHED))
            && !ContainerMutations.registry().isWatched(originOf(context, ALSO_WATCHED)),
        "An unpowered terminal should be listening to nothing");
    context.assertTrue(session.index().isEmpty(), "An unpowered terminal should hold no index");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void anUnpoweredTerminalIgnoresChangesEntirely(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    NetworkIndexSession<ItemVariant> session = poweredSession(context);
    session.index();
    session.end();
    int rebuildsWhilePowered = session.recomputeCount();

    BarrelBlockEntity barrel = context.getBlockEntity(WATCHED);
    barrel.setStack(1, new ItemStack(Items.DIAMOND, 5));
    session.index();

    context.assertTrue(
        session.recomputeCount() == rebuildsWhilePowered,
        "An unpowered terminal should do no work, rebuilt " + session.recomputeCount());
    context.complete();
  }

  private NetworkIndexSession<ItemVariant> poweredSession(TestContext context) {
    NetworkIndexSession<ItemVariant> session = NetworkIndexSession.forWorld(context.getWorld());
    session.begin(
        discovery.scan(context.getWorld(), new ScanRegion(context.getAbsolutePos(CENTRE), RADIUS)));
    return session;
  }

  private static AtomicInteger countMutationsOf(TestContext context, BlockPos pos) {
    AtomicInteger mutations = new AtomicInteger();
    ContainerMutations.registry()
        .watch(originOf(context, pos), origin -> mutations.incrementAndGet());
    return mutations;
  }

  private static StorageOrigin originOf(TestContext context, BlockPos pos) {
    return BlockStorageOrigin.of(context.getWorld(), context.getAbsolutePos(pos));
  }

  private static void stockBarrel(TestContext context, BlockPos pos, ItemStack... stacks) {
    context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(pos);
    for (int slot = 0; slot < stacks.length; slot++) {
      barrel.setStack(slot, stacks[slot]);
    }
  }
}
