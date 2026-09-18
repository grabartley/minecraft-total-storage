package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerFilterService;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterServiceGameTest implements FabricGameTest {
  private static final String BATCH = "container-filters";
  private static final int TICK_LIMIT = 60;

  private final ContainerFilterService filters = new ContainerFilterService();
  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilteredContainerStartsAcceptingNothing(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A container turned to filtered should accept nothing yet");
    context.assertTrue(rowFor(context).acceptsNothing(), "The row should say it accepts nothing");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanSayWhatAFilteredContainerAccepts(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    RoleChangeOutcome outcome = accept(context, owner, "ores_and_minerals", true);

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "The tag should have been written");
    context.assertTrue(
        filters
            .filterOf(context.getWorld(), terminal(context), east(context))
            .hasTag("ores_and_minerals"),
        "The container should now accept that tag");
    context.assertTrue(
        rowFor(context).accepts("ores_and_minerals"),
        "The row should carry it, got " + rowFor(context).acceptedTags());
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagCanBeTakenBackOffAgain(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    accept(context, owner, "ores_and_minerals", true);

    RoleChangeOutcome outcome = accept(context, owner, "ores_and_minerals", false);

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "Removing a tag should be a change");
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "The container should be back to accepting nothing");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void acceptingATagTwiceWritesNothingTheSecondTime(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    accept(context, owner, "food", true);

    RoleChangeOutcome outcome = accept(context, owner, "food", true);

    context.assertTrue(
        outcome == RoleChangeOutcome.UNCHANGED, "A repeat should say so, got " + outcome);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aBlankTagNameIsIgnoredRatherThanStored(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    RoleChangeOutcome outcome = accept(context, owner, "   ", true);

    context.assertTrue(
        outcome == RoleChangeOutcome.UNCHANGED,
        "A blank tag should change nothing, got " + outcome);
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A blank tag was stored anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clearingLeavesAFilteredContainerAcceptingNothing(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    accept(context, owner, "food", true);
    accept(context, owner, "brewing", true);

    RoleChangeOutcome outcome =
        filters.clear(
            context.getWorld(), terminal(context), TerminalCaller.of(owner), east(context));

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "Clearing should be a change");
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "The filter should be empty");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStrangerCannotSayWhatSomebodyElsesContainerAccepts(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    ServerPlayerEntity stranger = NetworkOperationFixture.standingPlayer(context);

    RoleChangeOutcome outcome =
        filters.accept(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(stranger, false),
            east(context),
            "food",
            true);

    context.assertTrue(outcome == RoleChangeOutcome.DENIED, "A stranger wrote, got " + outcome);
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A refused filter was written anyway");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGeneralContainerKeepsNoFilterAtAll(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    accept(context, owner, "food", true);

    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A general container should carry no filter");
    NetworkOperationFixture.finish(context, owner);
  }

  private RoleChangeOutcome accept(
      TestContext context, ServerPlayerEntity owner, String tagId, boolean accepted) {
    return filters.accept(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(owner),
        east(context),
        tagId,
        accepted);
  }

  private ServerPlayerEntity filteredEast(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(owner),
        east(context),
        ContainerRole.FILTERED);
    return owner;
  }

  private ContainerRoleEntry rowFor(TestContext context) {
    final List<ContainerRoleEntry> rows = roles.containers(context.getWorld(), terminal(context));
    return rows.stream()
        .filter(row -> row.container().equals(east(context)))
        .findFirst()
        .orElseThrow();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
