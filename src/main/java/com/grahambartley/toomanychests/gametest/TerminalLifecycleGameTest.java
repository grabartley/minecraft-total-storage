package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import com.grahambartley.toomanychests.terminal.TerminalLifecycle;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalLifecycleGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-lifecycle";

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aTerminalThatLoadsPoweredIsOwedAStartRatherThanGivenOne(TestContext context) {
    final ServerWorld world = context.getWorld();

    placeAlreadyPowered(context);

    context.assertTrue(
        TerminalNetworks.power().network(world, context.getAbsolutePos(TERMINAL)).isEmpty(),
        "Loading a powered terminal must not start its network where the load is announced");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aTerminalThatLoadsPoweredIsRunningOnceTheTickComes(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(TERMINAL);
    placeAlreadyPowered(context);

    NextTick.queue().runPending();

    context.assertTrue(
        TerminalNetworks.power().network(world, pos).isPresent(),
        "A terminal that loaded powered should be running once the tick it was owed arrives");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aTerminalThatGoesAgainBeforeTheTickIsNeverStarted(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(TERMINAL);
    placeAlreadyPowered(context);

    TerminalLifecycle.unloaded(world, pos);
    NextTick.queue().runPending();

    context.assertTrue(
        TerminalNetworks.power().network(world, pos).isEmpty(),
        "A terminal that unloaded again must not be woken by the tick it was owed");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aStartOwedToATerminalThatIsGoneFindsNothingToStart(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(TERMINAL);
    placeAlreadyPowered(context);
    final TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    tearDown(context);

    TerminalLifecycle.loaded(world, terminal);
    NextTick.queue().runPending();

    context.assertTrue(
        terminal.isRemoved(), "The terminal should have been marked removed by the world");
    context.assertTrue(
        TerminalNetworks.power().network(world, pos).isEmpty(),
        "A start owed to a terminal that has gone must find nothing to start");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aTerminalAnnouncedRepeatedlyStartsExactlyOneNetwork(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(TERMINAL);
    placeAlreadyPowered(context);
    final TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    TerminalLifecycle.loaded(world, terminal);
    TerminalLifecycle.loaded(world, terminal);

    NextTick.queue().runPending();
    final NetworkMembership started = TerminalNetworks.power().network(world, pos).orElse(null);
    context.assertTrue(started != null, "The terminal should have started a network");
    TerminalLifecycle.loaded(world, terminal);
    NextTick.queue().runPending();

    context.assertTrue(
        TerminalNetworks.power().network(world, pos).orElse(null) == started,
        "A further load announcement must not rebuild a network that is already running");
    context.assertTrue(
        ContainerLifecycle.registry().isSubscribed(started),
        "The one running network should still be listening for containers");
    TerminalLifecycle.unloaded(world, pos);
    context.assertFalse(
        ContainerLifecycle.registry().isSubscribed(started),
        "Tearing that network down should leave nothing listening behind it");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 40)
  public void aTerminalThatLoadsUnpoweredStaysInertWhenTheTickComes(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(TERMINAL);
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());

    NextTick.queue().runPending();

    context.assertTrue(
        TerminalNetworks.power().network(world, pos).isEmpty(),
        "A terminal that loaded unpowered should stay inert once its tick arrives");
    tearDown(context);
    context.complete();
  }

  private static BlockState poweredTerminal() {
    return ModBlocks.TERMINAL.getDefaultState().with(TerminalBlock.POWERED, Boolean.TRUE);
  }

  private void placeAlreadyPowered(TestContext context) {
    context.setBlockState(TERMINAL, poweredTerminal());
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(1));
  }

  private void tearDown(TestContext context) {
    context.setBlockState(TERMINAL, Blocks.AIR.getDefaultState());
  }
}
