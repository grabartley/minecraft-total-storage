package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.index.NetworkIndexSession;
import com.grahambartley.totalstorage.lifecycle.ContainerLifecycleRegistry;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerDiscoveryScan;
import com.grahambartley.totalstorage.storage.ScanRegion;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class NetworkMembershipGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "network-membership";

  private static final BlockPos CENTRE = new BlockPos(3, 1, 3);
  private static final int RADIUS = 1;
  private static final BlockPos WATCHED = new BlockPos(2, 1, 2);
  private static final BlockPos ALSO_WATCHED = new BlockPos(4, 1, 4);
  private static final BlockPos OUT_OF_RANGE = new BlockPos(6, 1, 6);
  private static final BlockPos CHEST_LEFT = new BlockPos(2, 1, 2);
  private static final BlockPos CHEST_RIGHT = new BlockPos(3, 1, 2);
  private static final int ADMISSION_TICKS = 2;

  private final StorageProviderResolver resolver = new StorageProviderResolver();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingAConnectedContainerTakesItsItemsOutOfTheListing(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.COBBLESTONE, 16));
    NetworkMembership membership = poweredNetwork(context);
    context.assertTrue(
        membership.index().countOf(ItemVariant.of(Items.DIAMOND)) == 8L,
        "The listing should start with the diamonds the barrel holds");

    context.setBlockState(WATCHED, Blocks.AIR.getDefaultState());

    long diamonds = membership.index().countOf(ItemVariant.of(Items.DIAMOND));
    context.assertTrue(
        diamonds == 0L, "A broken container should offer nothing, offered " + diamonds);
    context.assertTrue(
        membership.index().countOf(ItemVariant.of(Items.COBBLESTONE)) == 16L,
        "The rest of the network should be untouched by one container going");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingAConnectedContainerRemovesItFromTheNetwork(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.index();

    context.setBlockState(WATCHED, Blocks.AIR.getDefaultState());

    context.assertTrue(
        !membership.session().isWatching(originOf(context, WATCHED)),
        "A broken container should no longer be watched");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void replacingAConnectedContainerWithAnotherBlockDropsItToo(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.index();

    context.setBlockState(WATCHED, Blocks.STONE.getDefaultState());

    context.assertTrue(membership.index().isEmpty(), "A replaced container should offer nothing");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingAContainerOutsideTheNetworkChangesNothing(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    stockBarrel(context, OUT_OF_RANGE, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    int containersBefore = membership.containers().size();

    context.setBlockState(OUT_OF_RANGE, Blocks.AIR.getDefaultState());

    context.assertTrue(
        membership.containers().size() == containersBefore,
        "A container the network never held should not change it");
    context.assertTrue(
        membership.index().countOf(ItemVariant.of(Items.DIAMOND)) == 8L,
        "Only the connected barrel's diamonds should be listed");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aWithdrawalAgainstAContainerBrokenThisTickGrantsNothing(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.index();
    StorageProvider held =
        resolver.resolve(context.getWorld(), absolute(context, WATCHED)).orElseThrow();
    long reachableBefore = extractable(held);

    context.setBlockState(WATCHED, Blocks.AIR.getDefaultState());
    long granted = withdrawThroughRevalidation(context, held);

    context.assertTrue(
        reachableBefore == 8L,
        "The provider held from before the break must really have been able to extract, could take "
            + reachableBefore);
    context.assertTrue(granted == 0L, "A broken container must grant nothing, granted " + granted);
    context.assertTrue(
        membership.index().countOf(ItemVariant.of(Items.DIAMOND)) == 0L,
        "A broken container must not be listed for withdrawal either");
    context.assertTrue(
        diamondsInTheWorld(context) == 8,
        "Breaking a barrel and failing a withdrawal must conserve its diamonds, found "
            + diamondsInTheWorld(context));
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void unloadingTheChunkHoldingTheContainersEmptiesTheListing(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    ContainerLifecycleRegistry lifecycle = new ContainerLifecycleRegistry();
    NetworkMembership membership = poweredNetwork(context, lifecycle);
    membership.index();

    lifecycle.chunkUnloaded(worldKey(context), chunkOf(context, WATCHED));

    context.assertTrue(
        !membership.session().isWatching(originOf(context, WATCHED)),
        "An unloaded chunk's containers should leave the network");
    context.assertTrue(
        membership.index().countOf(ItemVariant.of(Items.DIAMOND)) == 0L,
        "An unloaded chunk's items should leave the listing");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void loadingTheChunkAgainPutsTheContainersBack(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    ContainerLifecycleRegistry lifecycle = new ContainerLifecycleRegistry();
    NetworkMembership membership = poweredNetwork(context, lifecycle);
    membership.index();
    lifecycle.chunkUnloaded(worldKey(context), chunkOf(context, WATCHED));

    lifecycle.chunkLoaded(worldKey(context), chunkOf(context, WATCHED));

    context.assertTrue(
        membership.session().isWatching(originOf(context, WATCHED)),
        "A reloaded chunk's containers should rejoin the network");
    long diamonds = membership.index().countOf(ItemVariant.of(Items.DIAMOND));
    context.assertTrue(
        diamonds == 8L, "A reloaded chunk's items should return, listed " + diamonds);
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aChunkLoadingOutsideTheRadiusLeavesTheNetworkAlone(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    ContainerLifecycleRegistry lifecycle = new ContainerLifecycleRegistry();
    NetworkMembership membership = poweredNetwork(context, lifecycle);
    membership.index();
    lifecycle.chunkUnloaded(worldKey(context), chunkOf(context, WATCHED));

    lifecycle.chunkLoaded(worldKey(context), distantChunk(context));

    context.assertTrue(
        !membership.session().isWatching(originOf(context, WATCHED)),
        "A chunk the terminal cannot reach should not make it re-scan");
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void anUnpoweredNetworkIgnoresTheWorldEntirely(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.powerOff();

    context.setBlockState(WATCHED, Blocks.AIR.getDefaultState());

    context.assertTrue(membership.index().isEmpty(), "An unpowered network holds no listing");
    context.assertTrue(
        membership.containers().isEmpty(), "An unpowered network holds no containers");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aContainerPlacedInRangeJoinsThePoweredNetworkWithoutARescan(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.index();

    stockBarrel(context, ALSO_WATCHED, new ItemStack(Items.COBBLESTONE, 16));

    context.waitAndRun(
        ADMISSION_TICKS,
        () -> {
          context.assertTrue(
              membership.session().isWatching(originOf(context, ALSO_WATCHED)),
              "A barrel placed in range should join the network");
          long cobblestone = membership.index().countOf(ItemVariant.of(Items.COBBLESTONE));
          context.assertTrue(
              cobblestone == 16L, "The new barrel's items should be listed, listed " + cobblestone);
          membership.powerOff();
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void turningASingleChestIntoADoubleChestLeavesOneContainerHoldingBoth(
      TestContext context) {
    context.setBlockState(CHEST_LEFT, chest(ChestType.SINGLE));
    ChestBlockEntity first = context.getBlockEntity(CHEST_LEFT);
    first.setStack(0, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    membership.index();

    context.setBlockState(CHEST_RIGHT, chest(ChestType.RIGHT));
    context.setBlockState(CHEST_LEFT, chest(ChestType.LEFT));
    ChestBlockEntity second = context.getBlockEntity(CHEST_RIGHT);
    second.setStack(0, new ItemStack(Items.DIAMOND, 5));

    context.waitAndRun(
        ADMISSION_TICKS,
        () -> {
          context.assertTrue(
              membership.containers().size() == 1,
              "A double chest is one container, the network holds "
                  + membership.containers().size());
          long diamonds = membership.index().countOf(ItemVariant.of(Items.DIAMOND));
          context.assertTrue(
              diamonds == 13L, "Both halves' diamonds should be listed, listed " + diamonds);
          membership.powerOff();
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aContainerPlacedOutOfRangeLeavesTheNetworkAlone(TestContext context) {
    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));
    NetworkMembership membership = poweredNetwork(context);
    int containersBefore = membership.containers().size();

    stockBarrel(context, OUT_OF_RANGE, new ItemStack(Items.DIAMOND, 8));

    context.waitAndRun(
        ADMISSION_TICKS,
        () -> {
          context.assertTrue(
              membership.containers().size() == containersBefore,
              "A barrel out of range should not join the network");
          context.assertTrue(
              membership.index().countOf(ItemVariant.of(Items.DIAMOND)) == 8L,
              "Only the connected barrel's diamonds should be listed");
          membership.powerOff();
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aContainerPlacedNextToAnUnpoweredNetworkChangesNothing(TestContext context) {
    NetworkMembership membership = poweredNetwork(context);
    membership.powerOff();

    stockBarrel(context, WATCHED, new ItemStack(Items.DIAMOND, 8));

    context.waitAndRun(
        ADMISSION_TICKS,
        () -> {
          context.assertTrue(
              membership.containers().isEmpty(), "An unpowered network should admit nothing");
          context.complete();
        });
  }

  private NetworkMembership poweredNetwork(TestContext context) {
    NetworkMembership membership = NetworkMembership.around(context.getWorld(), region(context));
    membership.powerOn();
    return membership;
  }

  private NetworkMembership poweredNetwork(
      TestContext context, ContainerLifecycleRegistry lifecycle) {
    NetworkMembership membership =
        new NetworkMembership(
            context.getWorld(),
            region(context),
            NetworkIndexSession.forWorld(context.getWorld()),
            new ContainerDiscoveryScan(),
            lifecycle);
    membership.powerOn();
    return membership;
  }

  private static ScanRegion region(TestContext context) {
    return new ScanRegion(context.getAbsolutePos(CENTRE), RADIUS);
  }

  private long extractable(StorageProvider provider) {
    try (Transaction probe = Transaction.openOuter()) {
      return provider.storage().extract(ItemVariant.of(Items.DIAMOND), 64L, probe);
    }
  }

  private long withdrawThroughRevalidation(TestContext context, StorageProvider held) {
    Optional<StorageProvider> live = resolver.revalidate(context.getWorld(), held);
    if (live.isEmpty()) {
      return 0L;
    }
    try (Transaction withdrawal = Transaction.openOuter()) {
      long taken = live.get().storage().extract(ItemVariant.of(Items.DIAMOND), 64L, withdrawal);
      withdrawal.commit();
      return taken;
    }
  }

  private static int diamondsInTheWorld(TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class, context.getTestBox(), item -> item.getStack().isOf(Items.DIAMOND))
        .stream()
        .mapToInt(item -> item.getStack().getCount())
        .sum();
  }

  private static ChunkPos chunkOf(TestContext context, BlockPos pos) {
    return new ChunkPos(context.getAbsolutePos(pos));
  }

  private static ChunkPos distantChunk(TestContext context) {
    ChunkPos here = chunkOf(context, CENTRE);
    return new ChunkPos(here.x + 32, here.z + 32);
  }

  private static RegistryKey<World> worldKey(TestContext context) {
    return context.getWorld().getRegistryKey();
  }

  private static StorageOrigin originOf(TestContext context, BlockPos pos) {
    return BlockStorageOrigin.of(context.getWorld(), context.getAbsolutePos(pos));
  }

  private static BlockPos absolute(TestContext context, BlockPos pos) {
    return context.getAbsolutePos(pos);
  }

  private static void stockBarrel(TestContext context, BlockPos pos, ItemStack... stacks) {
    context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(pos);
    for (int slot = 0; slot < stacks.length; slot++) {
      barrel.setStack(slot, stacks[slot]);
    }
  }

  private static BlockState chest(ChestType type) {
    return Blocks.CHEST
        .getDefaultState()
        .with(ChestBlock.FACING, Direction.NORTH)
        .with(ChestBlock.CHEST_TYPE, type);
  }
}
