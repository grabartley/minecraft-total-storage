package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkCapacityService;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * How much room the panel says a network has left.
 *
 * <p>Counted from live containers, so only a world can answer it. The number exists to stop a
 * player discovering they are full on a failed deposit, which makes the case that matters most the
 * one where it must not lie: a container the network may not write to is not room.
 */
public final class NetworkCapacityServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-capacity";
  private static final int TICK_LIMIT = 60;

  /** A vanilla barrel, which is what every count here is a multiple of. */
  private static final int BARREL_SLOTS = 27;

  private final NetworkCapacityService capacity = new NetworkCapacityService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEmptyNetworkIsAllRoomAndNoneOfItUsed(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(
        room.totalSlots() == BARREL_SLOTS, "Counted " + room.totalSlots() + " of 27 slots");
    context.assertTrue(room.usedSlots() == 0, "Counted " + room.usedSlots() + " used in an empty");
    context.assertTrue(room.freeSlots() == BARREL_SLOTS, "All of it should be free");
    NetworkOperationFixture.finish(context, player);
  }

  /** Slots rather than items: one item in a slot spends the whole slot. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSlotHoldingOneItemIsAsUsedAsOneHoldingSixtyFour(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 1);
    barrel.setStack(1, new ItemStack(Items.COBBLESTONE, 64));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(room.usedSlots() == 2, "Counted " + room.usedSlots() + " of two used slots");
    context.assertTrue(
        room.freeSlots() == BARREL_SLOTS - 2, "Counted " + room.freeSlots() + " free");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyContainerTheNetworkMayWriteToIsCounted(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(
        room.totalSlots() == 2 * BARREL_SLOTS,
        "Two barrels should count " + (2 * BARREL_SLOTS) + " slots, counted " + room.totalSlots());
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * An excluded container is not room. Counting it would make the readout drift further from the
   * truth with every chest a player deliberately protected, which is the opposite of what it is
   * for.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsNeitherRoomNorFullness(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(
        room.totalSlots() == BARREL_SLOTS,
        "Only the writable barrel counts, counted " + room.totalSlots());
    NetworkOperationFixture.finish(context, player);
  }

  /** A network with every slot spoken for is full, which is what the warning colour is for. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkWithEverySlotSpokenForReadsAsFull(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(room.isFull(), "A barrel with every slot filled should read as full");
    context.assertTrue(room.percentUsed() == 100, "Read " + room.percentUsed() + " percent");
    NetworkOperationFixture.finish(context, player);
  }

  /** No containers is not the same as no room, and only one of them is a player's problem. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkReachingNothingIsUnknownRatherThanFull(TestContext context) {
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity room = capacityNow(context);

    context.assertTrue(room.isUnknown(), "A network with no containers knows no capacity");
    context.assertTrue(!room.isFull(), "Reaching nothing is not being full");
    NetworkOperationFixture.finish(context, player);
  }

  private NetworkCapacity capacityNow(TestContext context) {
    final BlockPos terminal = context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
    return TerminalNetworks.power()
        .network(context.getWorld(), terminal)
        .map(network -> capacity.of(context.getWorld(), terminal, network))
        .orElse(NetworkCapacity.EMPTY);
  }
}
