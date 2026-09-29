package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.HighlightedContainer;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerHighlightServiceGameTest implements FabricGameTest {
  private static final String BATCH = "container-highlight";
  private static final int TICK_LIMIT = 60;

  private static final BlockPos DISTANT = new BlockPos(6, 1, 6);
  private static final int FAR_AWAY_BLOCKS = 24;
  private static final BlockPos OUTSIDE_THE_RADIUS = new BlockPos(3, 1, 6);

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

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void poweringATerminalOwesItsNeighboursOneHighlight(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = unpoweredTerminal(context);

    context.setBlockState(NetworkOperationFixture.POWER, Blocks.REDSTONE_BLOCK.getDefaultState());

    context.assertTrue(
        NextTick.queue().isPending(powerOnHighlight(context)),
        "Turning the signal on should have queued a highlight for the next tick");
    NextTick.queue().cancel(powerOnHighlight(context));
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTerminalThatLoadsAlreadyPoweredOwesNoHighlight(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));
    NextTick.queue().cancel(powerOnHighlight(context));

    TerminalNetworks.power()
        .loaded(
            context.getWorld(),
            terminal(context),
            context.getBlockState(NetworkOperationFixture.TERMINAL));

    context.assertFalse(
        NextTick.queue().isPending(powerOnHighlight(context)),
        "A terminal loading with its signal already on must not outline anything");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerStandingByIsOutlinedAndNobodyElseIs(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = unpoweredTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.standingPlayer(context);
    ServerPlayerEntity farAway = NetworkOperationFixture.standingPlayer(context);
    BlockPos far = terminal(context).add(FAR_AWAY_BLOCKS, 0, 0);
    farAway.refreshPositionAndAngles(far.getX() + 0.5, far.getY(), far.getZ() + 0.5, 0f, 0f);
    context
        .<TerminalBlockEntity>getBlockEntity(NetworkOperationFixture.TERMINAL)
        .updateSettings(settings -> settings.withAccessMode(TerminalAccessMode.OWNER));
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
    NextTick.queue().cancel(powerOnHighlight(context));

    List<ServerPlayerEntity> outlined =
        ContainerHighlightService.highlightForEveryoneNearby(context.getWorld(), terminal(context));

    context.assertTrue(
        outlined.equals(List.of(owner)),
        "Only the owner standing by should be outlined, got "
            + outlined.stream().map(player -> player.getUuid().toString()).toList());
    NetworkOperationFixture.finish(context, owner, guest, farAway);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWithinReachButOutsideTheRadiusIsOutlined(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = unpoweredTerminal(context);
    BlockPos justOutside = context.getAbsolutePos(OUTSIDE_THE_RADIUS);
    owner.refreshPositionAndAngles(
        justOutside.getX() + 0.5, justOutside.getY(), justOutside.getZ() + 0.5, 0f, 0f);
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
    NextTick.queue().cancel(powerOnHighlight(context));

    List<ServerPlayerEntity> outlined =
        ContainerHighlightService.highlightForEveryoneNearby(context.getWorld(), terminal(context));

    context.assertTrue(
        outlined.equals(List.of(owner)),
        "A player within reach of the lever should be outlined even outside the radius");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalOutlinesNobody(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = unpoweredTerminal(context);

    List<ServerPlayerEntity> outlined =
        ContainerHighlightService.highlightForEveryoneNearby(context.getWorld(), terminal(context));

    context.assertTrue(outlined.isEmpty(), "An unpowered terminal outlined " + outlined.size());
    NetworkOperationFixture.finish(context, owner);
  }

  private static ServerPlayerEntity unpoweredTerminal(TestContext context) {
    ServerPlayerEntity owner = NetworkOperationFixture.standingPlayer(context);
    context.setBlockState(NetworkOperationFixture.TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        terminal(context),
        context.getBlockState(NetworkOperationFixture.TERMINAL),
        owner,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context
        .<TerminalBlockEntity>getBlockEntity(NetworkOperationFixture.TERMINAL)
        .updateSettings(settings -> settings.withRadius(1));
    return owner;
  }

  private static ContainerHighlightService.PowerOnHighlight powerOnHighlight(TestContext context) {
    return new ContainerHighlightService.PowerOnHighlight(
        TerminalAddress.of(context.getWorld(), terminal(context)));
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
