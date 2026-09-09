package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.index.NetworkIndexSession;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycleRegistry;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.terminal.ContainerRoleEviction;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class ContainerRoleEvictionGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "container-roles";
  private static final int TICK_LIMIT = 40;
  private static final int RADIUS = 2;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);
  private static final BlockPos OTHER_BARREL = new BlockPos(2, 1, 3);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void breakingAContainerForgetsTheRoleItWasGiven(TestContext context) {
    placeBarrel(context, BARREL);
    placePoweredTerminal(context);
    exclude(context, BARREL);

    context.setBlockState(BARREL, Blocks.AIR.getDefaultState());

    context.assertTrue(
        roleOf(context, BARREL) == ContainerRole.GENERAL,
        "A broken container should leave no role behind, left " + roleOf(context, BARREL));
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aContainerReplacedAtTheSamePositionStartsAtTheDefaultRole(TestContext context) {
    placeBarrel(context, BARREL);
    placePoweredTerminal(context);
    exclude(context, BARREL);

    context.setBlockState(BARREL, Blocks.AIR.getDefaultState());
    placeBarrel(context, BARREL);

    context.assertTrue(
        roleOf(context, BARREL) == ContainerRole.GENERAL,
        "A replacement container should start general, started " + roleOf(context, BARREL));
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void breakingAFilteredContainerForgetsWhatItAccepted(TestContext context) {
    placeBarrel(context, BARREL);
    placePoweredTerminal(context);
    terminal(context)
        .updateSettings(
            settings ->
                settings
                    .withContainerRole(absolute(context, BARREL), ContainerRole.FILTERED)
                    .withContainerFilter(
                        absolute(context, BARREL), ContainerFilter.ofTags("ores")));

    context.setBlockState(BARREL, Blocks.AIR.getDefaultState());

    context.assertTrue(
        terminal(context).settings().filterFor(absolute(context, BARREL)).isEmpty(),
        "A broken container should leave nothing it was told to accept behind");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void breakingOneContainerLeavesEveryOtherRoleAlone(TestContext context) {
    placeBarrel(context, BARREL);
    placeBarrel(context, OTHER_BARREL);
    placePoweredTerminal(context);
    exclude(context, BARREL);
    exclude(context, OTHER_BARREL);

    context.setBlockState(BARREL, Blocks.AIR.getDefaultState());

    context.assertTrue(
        roleOf(context, OTHER_BARREL) == ContainerRole.EXCLUDED,
        "One container going should not cost another its role");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aContainerLeavingWithItsChunkKeepsItsRole(TestContext context) {
    placeBarrel(context, BARREL);
    placeTerminal(context);
    exclude(context, BARREL);
    ContainerLifecycleRegistry lifecycle = new ContainerLifecycleRegistry();
    NetworkMembership membership = poweredNetwork(context, lifecycle);

    lifecycle.chunkUnloaded(
        context.getWorld().getRegistryKey(), new ChunkPos(absolute(context, BARREL)));

    context.assertTrue(
        roleOf(context, BARREL) == ContainerRole.EXCLUDED,
        "An unloaded chunk should not cost its containers their roles, left "
            + roleOf(context, BARREL));
    membership.powerOff();
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTerminalWithNoNetworkRunningHearsNothing(TestContext context) {
    placeBarrel(context, BARREL);
    placeTerminal(context);
    exclude(context, BARREL);

    context.setBlockState(BARREL, Blocks.AIR.getDefaultState());

    context.assertTrue(
        roleOf(context, BARREL) == ContainerRole.EXCLUDED,
        "An unpowered terminal is absent rather than idle and should hear no removal");
    context.complete();
  }

  private static NetworkMembership poweredNetwork(
      TestContext context, ContainerLifecycleRegistry lifecycle) {
    NetworkMembership membership =
        new NetworkMembership(
            context.getWorld(),
            new ScanRegion(absolute(context, TERMINAL), RADIUS),
            NetworkIndexSession.forWorld(context.getWorld()),
            new ContainerDiscoveryScan(),
            lifecycle);
    membership.powerOn();
    return membership;
  }

  private static void placeTerminal(TestContext context) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    terminal(context).updateSettings(settings -> settings.withRadius(RADIUS));
  }

  private static void placePoweredTerminal(TestContext context) {
    placeTerminal(context);
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
  }

  private static void placeBarrel(TestContext context, BlockPos pos) {
    context.setBlockState(pos, Blocks.BARREL.getDefaultState());
  }

  private static void exclude(TestContext context, BlockPos container) {
    terminal(context)
        .updateSettings(
            settings ->
                settings.withContainerRole(absolute(context, container), ContainerRole.EXCLUDED));
  }

  private static ContainerRole roleOf(TestContext context, BlockPos container) {
    return terminal(context).settings().roleFor(absolute(context, container));
  }

  private static TerminalBlockEntity terminal(TestContext context) {
    return context.getBlockEntity(TERMINAL);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRemovalNeverLoadsTheChunkTheTerminalIsIn(TestContext context) {
    final BlockPos away = farFromHere(context);
    final ChunkPos awayChunk = new ChunkPos(away);
    context.assertFalse(
        context.getWorld().getChunkManager().isChunkLoaded(awayChunk.x, awayChunk.z),
        "The chunk this test relies on being absent is already loaded");
    final ContainerRoleEviction eviction = new ContainerRoleEviction(context.getWorld(), away);

    eviction.containerRemoved(
        new BlockStorageOrigin(context.getWorld().getRegistryKey(), absolute(context, BARREL)));

    context.assertFalse(
        context.getWorld().getChunkManager().isChunkLoaded(awayChunk.x, awayChunk.z),
        "Forgetting a role must not pull the terminal's chunk back into the world");
    context.complete();
  }

  private static BlockPos farFromHere(TestContext context) {
    final BlockPos here = context.getAbsolutePos(BlockPos.ORIGIN);
    return new BlockPos(here.getX() + 2048, here.getY(), here.getZ() + 2048);
  }

  private static BlockPos absolute(TestContext context, BlockPos pos) {
    return context.getAbsolutePos(pos);
  }

  private static void tearDown(TestContext context) {
    TerminalNetworks.power().unloaded(context.getWorld(), absolute(context, TERMINAL));
  }
}
