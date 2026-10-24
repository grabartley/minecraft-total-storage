package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerAssignmentWriter;
import com.grahambartley.totalstorage.terminal.RoleChangeOutcome;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerAssignmentWriterGameTest implements FabricGameTest {
  private static final String BATCH = "container-assignment-writer";
  private static final int TICK_LIMIT = 60;

  private final ContainerAssignmentWriter writer = new ContainerAssignmentWriter();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOwnersChangeIsWrittenToTheTerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(owner),
            east(context),
            assignment -> assignment.withName("Potions"));

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "The change should have landed");
    context.assertTrue(
        "Potions".equals(nameOf(context)), "The name should have stuck, got " + nameOf(context));
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aChangeThatAltersNothingSaysSoRatherThanRewriting(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(owner),
            east(context),
            assignment -> assignment);

    context.assertTrue(
        outcome == RoleChangeOutcome.UNCHANGED, "An idle change should say so, got " + outcome);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void somebodyElsesTerminalRefusesTheChange(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity stranger = NetworkOperationFixture.standingPlayer(context);

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(stranger, false),
            east(context),
            assignment -> assignment.withName("Mine now"));

    context.assertTrue(outcome == RoleChangeOutcome.DENIED, "A stranger wrote, got " + outcome);
    context.assertTrue(nameOf(context).isEmpty(), "A refused change was written anyway");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerTheNetworkDoesNotReachIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(owner),
            context.getAbsolutePos(NetworkOperationFixture.FAR_BARREL),
            assignment -> assignment.withRole(ContainerRole.EXCLUDED));

    context.assertTrue(
        outcome == RoleChangeOutcome.NOT_IN_NETWORK,
        "An unreached container should be refused, got " + outcome);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aChangeAtAPlaceWithNoTerminalIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            east(context),
            TerminalCaller.of(owner),
            east(context),
            assignment -> assignment.withName("Nowhere"));

    context.assertTrue(
        outcome == RoleChangeOutcome.NO_TERMINAL,
        "A change with no terminal should be refused, got " + outcome);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aChangeReturningNothingLeavesTheContainerAtTheDefault(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    writer.write(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(owner),
        east(context),
        assignment -> assignment.withName("Potions"));

    RoleChangeOutcome outcome =
        writer.write(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(owner),
            east(context),
            assignment -> null);

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "The reset should have landed");
    context.assertTrue(
        assignmentOf(context).isDefault(), "A null change should read as the default assignment");
    NetworkOperationFixture.finish(context, owner);
  }

  private static ContainerAssignment assignmentOf(TestContext context) {
    return TerminalLookup.settingsAt(context.getWorld(), terminal(context))
        .map(settings -> settings.assignmentFor(east(context)))
        .orElse(ContainerAssignment.DEFAULT);
  }

  private static String nameOf(TestContext context) {
    return Optional.of(assignmentOf(context)).map(ContainerAssignment::name).orElse("");
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
