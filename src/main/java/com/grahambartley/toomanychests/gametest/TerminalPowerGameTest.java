package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.ModItems;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.TerminalLifecycle;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalPower;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * What redstone does to a terminal, driven through real blocks so the neighbour updates arrive the
 * way the world delivers them rather than being called directly.
 *
 * <p>Per ADR 0007 an unpowered terminal is absent rather than idle, so most of what is proven here
 * is negative: that nothing is listening, nothing is indexed, and nothing rescans.
 *
 * <p>Each terminal is given a radius of one before it is powered, so a scan cannot wander into a
 * neighbouring test's structure. Chunk loading is driven through {@link TerminalPower} directly
 * here, which is what makes these power tests readable; that a real chunk load reaches the same
 * place is proven by {@code TerminalLifecycleGameTest}, out in a chunk it is allowed to unload.
 */
public final class TerminalPowerGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-power";

  private static final BlockPos FLOOR = new BlockPos(3, 0, 3);
  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos OTHER_POWER = new BlockPos(3, 1, 4);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void redstonePowersTheTerminalOn(TestContext context) {
    placeTerminal(context);

    switchOn(context);

    context.assertTrue(
        context.getBlockState(TERMINAL).get(TerminalBlock.POWERED),
        "A terminal reached by redstone should read as powered");
    context.assertTrue(network(context) != null, "A powered terminal should be running a network");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void poweringOnFindsTheContainersInReach(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);

    switchOn(context);

    long diamonds = network(context).index().countOf(ItemVariant.of(Items.DIAMOND));
    context.assertTrue(
        diamonds == 8L, "Powering on should index the barrel beside it, listed " + diamonds);
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void anUnpoweredTerminalRunsNoNetwork(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));

    placeTerminal(context);

    context.assertFalse(
        context.getBlockState(TERMINAL).get(TerminalBlock.POWERED),
        "A terminal with no redstone reaching it should read as unpowered");
    context.assertTrue(
        network(context) == null, "An unpowered terminal should be running no network at all");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void usingAnUnpoweredTerminalStartsNothing(TestContext context) {
    placeTerminal(context);
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    context.useBlock(TERMINAL, player);

    context.assertTrue(
        network(context) == null, "Right-clicking an unpowered terminal must not wake it up");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void cuttingPowerLeavesTheTerminalUnpowered(TestContext context) {
    placeTerminal(context);
    switchOn(context);
    context.assertTrue(
        network(context) != null, "The terminal should have powered on before power is cut");

    switchOff(context);

    context.assertFalse(
        context.getBlockState(TERMINAL).get(TerminalBlock.POWERED),
        "A terminal whose redstone has gone should read as unpowered");
    context.assertTrue(network(context) == null, "Cutting power should leave no network behind");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void cuttingPowerLeavesNothingListening(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);
    switchOn(context);
    NetworkMembership running = network(context);
    running.index();

    switchOff(context);

    context.assertFalse(
        ContainerLifecycle.registry().isSubscribed(running),
        "An unpowered terminal must not still be listening for containers");
    context.assertFalse(
        running.session().isActive(), "An unpowered terminal must hold no index session");
    context.assertTrue(running.index().isEmpty(), "An unpowered terminal must offer nothing");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 60)
  public void togglingPowerRapidlyLeavesNothingListening(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);
    List<NetworkMembership> everyNetwork = new ArrayList<>();

    for (int cycle = 0; cycle < 5; cycle++) {
      switchOn(context);
      NetworkMembership started = network(context);
      context.assertTrue(started != null, "Every power cycle should really have started a network");
      everyNetwork.add(started);
      switchOff(context);
    }

    context.assertTrue(network(context) == null, "The last power cut should leave no network");
    context.assertTrue(
        everyNetwork.stream().distinct().count() == 5L,
        "Each power cycle should build its own network rather than reviving the last one");
    for (final NetworkMembership network : everyNetwork) {
      context.assertFalse(
          ContainerLifecycle.registry().isSubscribed(network),
          "No network from any power cycle may still be listening");
    }
    context.complete();
  }

  /**
   * Redstone reaching a terminal from a second direction is a neighbour update against a terminal
   * that is already on. The scan is the most expensive thing the mod does, so it must not happen
   * again: a fresh scan would re-track every container and re-read them all on the next listing.
   */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void furtherRedstoneDoesNotMakeAPoweredTerminalScanAgain(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);
    switchOn(context);
    NetworkMembership running = network(context);
    running.index();
    int readsAfterFirstScan = running.session().containerReadCount();

    context.setBlockState(OTHER_POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
    context.setBlockState(OTHER_POWER, Blocks.AIR.getDefaultState());

    context.assertTrue(
        network(context) == running,
        "A terminal that was already powered should still be running the same network");
    network(context).index();
    int readsNow = running.session().containerReadCount();
    context.assertTrue(
        readsNow == readsAfterFirstScan,
        "Redundant neighbour updates should cost no container reads, went from "
            + readsAfterFirstScan
            + " to "
            + readsNow);
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void placingATerminalIntoAPoweredPositionStartsItRunning(TestContext context) {
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(new BlockPos(3, 1, 5));
    player.refreshPositionAndAngles(
        standing.getX() + 0.5,
        standing.getY(),
        standing.getZ() + 0.5,
        Direction.SOUTH.asRotation(),
        0.0f);

    context.useStackOnBlock(player, new ItemStack(ModItems.TERMINAL), FLOOR, Direction.UP);

    context.assertTrue(
        context.getBlockState(TERMINAL).get(TerminalBlock.POWERED),
        "A terminal placed where redstone already reaches should place powered");
    // Placement reaches the terminal as a block entity load, which per ADR 0026 is owed a start
    // on the next tick rather than given one where it is announced.
    context.runAtTick(
        2L,
        () -> {
          context.assertTrue(
              network(context) != null,
              "A terminal placed into a powered position should be running on the next tick");
          tearDown(context);
          context.complete();
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aPoweredTerminalThatLoadsAgainStartsItsNetworkAgain(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);
    switchOn(context);
    BlockPos terminalPos = context.getAbsolutePos(TERMINAL);

    TerminalNetworks.power().unloaded(context.getWorld(), terminalPos);
    context.assertTrue(network(context) == null, "An unloaded terminal should be running nothing");
    TerminalNetworks.power()
        .loaded(context.getWorld(), terminalPos, context.getBlockState(TERMINAL));

    NetworkMembership reloaded = network(context);
    context.assertTrue(
        reloaded != null, "A terminal that loads powered should start its network again");
    context.assertTrue(
        reloaded.index().countOf(ItemVariant.of(Items.DIAMOND)) == 8L,
        "A reloaded terminal should scan its containers again");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void anUnpoweredTerminalThatLoadsAgainStartsNothing(TestContext context) {
    placeTerminal(context);
    BlockPos terminalPos = context.getAbsolutePos(TERMINAL);

    TerminalNetworks.power()
        .loaded(context.getWorld(), terminalPos, context.getBlockState(TERMINAL));

    context.assertTrue(
        network(context) == null, "A terminal that loads unpowered should stay inert");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void breakingAPoweredTerminalTearsDownItsNetwork(TestContext context) {
    stockBarrel(context, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context);
    switchOn(context);
    NetworkMembership running = network(context);
    context.assertTrue(
        running != null && ContainerLifecycle.registry().isSubscribed(running),
        "The terminal should have been running and listening before it was broken");

    context.getWorld().breakBlock(context.getAbsolutePos(TERMINAL), true);

    context.assertTrue(
        network(context) == null, "A broken terminal should leave no network running");
    context.assertFalse(
        ContainerLifecycle.registry().isSubscribed(running),
        "A broken terminal must not still be listening for containers");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void everySettingSurvivesARealPowerCycle(TestContext context) {
    placeTerminal(context);
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(3).withTag("ores"));

    switchOn(context);
    context.assertTrue(network(context) != null, "The terminal should really have powered on");
    switchOff(context);

    TerminalBlockEntity afterPowerCut = context.getBlockEntity(TERMINAL);
    context.assertTrue(
        afterPowerCut.settings().radius() == 3 && afterPowerCut.settings().hasTag("ores"),
        "Cutting power is an off switch, not a factory reset");
    context.complete();
  }

  private void placeTerminal(TestContext context) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(1));
  }

  private void switchOn(TestContext context) {
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
  }

  private void switchOff(TestContext context) {
    context.setBlockState(POWER, Blocks.AIR.getDefaultState());
  }

  private NetworkMembership network(TestContext context) {
    return TerminalNetworks.power()
        .network(context.getWorld(), context.getAbsolutePos(TERMINAL))
        .orElse(null);
  }

  /**
   * Tests that end with a terminal still powered hand the registry back the way they found it,
   * through the lifecycle rather than through {@link TerminalPower}, so a start still owed for the
   * terminal is withdrawn as well as the network torn down.
   */
  private void tearDown(TestContext context) {
    TerminalLifecycle.unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
  }

  private static void stockBarrel(TestContext context, ItemStack... stacks) {
    context.setBlockState(BARREL, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    for (int slot = 0; slot < stacks.length; slot++) {
      barrel.setStack(slot, stacks[slot]);
    }
  }
}
