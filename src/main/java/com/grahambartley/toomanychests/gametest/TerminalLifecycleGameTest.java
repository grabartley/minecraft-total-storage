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
import net.minecraft.util.math.ChunkPos;

/**
 * When a terminal that loaded powered actually starts running.
 *
 * <p>Per ADR 0026 a load is answered on the next tick rather than where the world announces it, so
 * every test here is about that gap: nothing may start inside the callback, what is owed must
 * happen once the tick comes, and a terminal that has gone again in between is left alone.
 *
 * <p>The terminals are placed already powered, which is a state only a saved world produces, and
 * the state that used to hang a world load. Every load is announced by the world itself rather than
 * by calling {@link com.grahambartley.toomanychests.terminal.TerminalPower} by hand, since calling
 * it by hand is what let the hang ship.
 *
 * <p>The first test unloads and reloads a chunk for real, out in a chunk a thousand blocks from the
 * test structures. That distance is the only way a gametest can unload a chunk at all: it cannot
 * unload the one it is standing in.
 */
public final class TerminalLifecycleGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-lifecycle";

  private static final int REMOTE_OFFSET = 1024;
  private static final int REMOTE_HEIGHT = 8;
  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 400)
  public void aTerminalSavedPoweredStartsItsNetworkWhenItsChunkLoadsAgain(TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = remoteTerminalPos(context);
    final ChunkPos chunkPos = new ChunkPos(pos);

    world.setChunkForced(chunkPos.x, chunkPos.z, true);
    world.setBlockState(pos, poweredTerminal());
    narrowTo(world, pos);
    world.setChunkForced(chunkPos.x, chunkPos.z, false);

    final long[] reloadedAt = {-1L};
    context.runAtEveryTick(
        () -> {
          if (reloadedAt[0] < 0L) {
            // The chunk going is a precondition, not the thing under test, and it arrives over
            // several ticks: the chunk manager lets go of a chunk before the unload it causes has
            // been announced. Waiting for both is what makes the reload below mean anything.
            if (world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)
                || TerminalNetworks.power().network(world, pos).isPresent()) {
              return;
            }

            world.setChunkForced(chunkPos.x, chunkPos.z, true);

            context.assertTrue(
                TerminalNetworks.power().network(world, pos).isEmpty(),
                "A chunk load must not start a network from inside the chunk load");
            reloadedAt[0] = context.getTick();
            return;
          }
          if (context.getTick() <= reloadedAt[0] + 1L) {
            return;
          }
          final NetworkMembership reloaded =
              TerminalNetworks.power().network(world, pos).orElse(null);
          context.assertTrue(
              reloaded != null,
              "A terminal saved powered should run a network the tick after its chunk loaded");
          context.assertTrue(
              ContainerLifecycle.registry().isSubscribed(reloaded),
              "A terminal that started again should be listening for containers again");
          discard(world, pos, chunkPos);
          context.complete();
        });
  }

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

  /**
   * The world marks a block entity removed when it is broken and when its chunk goes, so a start
   * owed to one is answered by asking it rather than by looking its position up again.
   */
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

  /**
   * A thousand blocks out, so the chunk is neither a spawn chunk nor one a mock player left over
   * from another test is holding loaded, and offset from this structure so it belongs to this test
   * alone.
   */
  private static BlockPos remoteTerminalPos(TestContext context) {
    final BlockPos here = context.getAbsolutePos(BlockPos.ORIGIN);
    return new BlockPos(
        here.getX() + REMOTE_OFFSET, here.getY() + REMOTE_HEIGHT, here.getZ() + REMOTE_OFFSET);
  }

  private static BlockState poweredTerminal() {
    return ModBlocks.TERMINAL.getDefaultState().with(TerminalBlock.POWERED, Boolean.TRUE);
  }

  /** A radius of one, so a scan out in the wild cannot walk into anything it does not own. */
  private static void narrowTo(ServerWorld world, BlockPos pos) {
    if (world.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
      terminal.updateSettings(settings -> settings.withRadius(1));
    }
  }

  private static void discard(ServerWorld world, BlockPos pos, ChunkPos chunkPos) {
    world.setBlockState(pos, Blocks.AIR.getDefaultState());
    world.setChunkForced(chunkPos.x, chunkPos.z, false);
  }

  private void placeAlreadyPowered(TestContext context) {
    context.setBlockState(TERMINAL, poweredTerminal());
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(1));
  }

  /** Removing the terminal is what the world would do, and it takes any network with it. */
  private void tearDown(TestContext context) {
    context.setBlockState(TERMINAL, Blocks.AIR.getDefaultState());
  }
}
