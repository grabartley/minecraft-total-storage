package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.HighlightedContainer;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Which containers a terminal says it is holding, which is the answer the world outlines.
 *
 * <p>This is the mod's answer to the one question a radius always produces. It has to name exactly
 * what the network holds: a container just outside the reach that lit up would teach a player the
 * wrong radius, and one inside it that did not would send them looking for a bug.
 */
public final class ContainerHighlightServiceGameTest implements FabricGameTest {
  private static final String BATCH = "container-highlight";
  private static final int TICK_LIMIT = 60;

  /** Outside the terminal's radius of one, so the network never sees it. */
  private static final BlockPos DISTANT = new BlockPos(6, 1, 6);

  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyContainerInRangeIsNamedAndNothingElseIs(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    NetworkOperationFixture.placeBarrel(context, DISTANT);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    List<HighlightedContainer> shown =
        ContainerHighlightService.containersOf(context.getWorld(), terminal(context));

    context.assertTrue(shown.size() == 2, "Named " + shown.size() + " of the two in range");
    context.assertFalse(
        shown.stream().anyMatch(row -> row.container().equals(context.getAbsolutePos(DISTANT))),
        "A container out of range was named");
    NetworkOperationFixture.finish(context, player);
  }

  /** ADR 0007: an unpowered terminal is holding nothing, so the empty answer is the true one. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalNamesNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.AIR);
    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));

    context.assertTrue(
        ContainerHighlightService.containersOf(context.getWorld(), terminal(context)).isEmpty(),
        "An unpowered terminal named containers");
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * The role travels with the position because seeing which chests are excluded is the reassurance
   * the highlight exists to give, and it cannot be read off a position.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void eachOutlineCarriesTheRoleItsContainerWasGiven(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(player),
        east(context),
        ContainerRole.EXCLUDED);

    List<HighlightedContainer> shown =
        ContainerHighlightService.containersOf(context.getWorld(), terminal(context));

    HighlightedContainer excluded =
        shown.stream()
            .filter(row -> row.container().equals(east(context)))
            .findFirst()
            .orElseThrow();
    context.assertTrue(
        excluded.role() == ContainerRole.EXCLUDED, "The outline missed the excluded role");
    context.assertTrue(
        shown.stream().filter(row -> row.role() == ContainerRole.GENERAL).count() == 1,
        "The other container should still read as general");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aHighlightIsSentToThePlayerWhoAskedForIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    int named = ContainerHighlightService.highlight(player, context.getWorld(), terminal(context));

    context.assertTrue(named == 1, "Named " + named + " of the one container in range");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPositionHoldingNoTerminalNamesNothingRatherThanThrowing(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    context.assertTrue(
        ContainerHighlightService.containersOf(context.getWorld(), context.getAbsolutePos(DISTANT))
            .isEmpty(),
        "A position holding no terminal named containers");
    context.getWorld().getServer().getPlayerManager().remove(player);
    context.complete();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
