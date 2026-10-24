package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerFilterService;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterCommandGameTest implements FabricGameTest {
  private static final String BATCH = "container-filter-command";
  private static final int TICK_LIMIT = 60;
  private static final int REFUSED = -1;

  private final ContainerFilterService filters = new ContainerFilterService();
  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanSayWhatAFilteredContainerAccepts(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    int result = runAsConsole(context, filterCommand(context, "add food"));

    context.assertTrue(result > 0, "Adding a tag was refused, got " + result);
    context.assertTrue(accepts(context, "food"), "The tag did not stick");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagCanBeTakenBackOffFromTheCommandTree(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    runAsConsole(context, filterCommand(context, "add food"));

    int result = runAsConsole(context, filterCommand(context, "remove food"));

    context.assertTrue(result > 0, "Removing a tag was refused, got " + result);
    context.assertTrue(!accepts(context, "food"), "The tag is still accepted");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagTheNetworkDoesNotKnowIsRefused(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    int result = runAsConsole(context, filterCommand(context, "add not-a-tag"));

    context.assertTrue(result <= 0, "An unknown tag was accepted, got " + result);
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "An unknown tag was written anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clearingLeavesTheContainerAcceptingNothing(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);
    runAsConsole(context, filterCommand(context, "add food"));

    int result = runAsConsole(context, filterCommand(context, "clear"));

    context.assertTrue(result > 0, "Clearing was refused, got " + result);
    context.assertTrue(
        filters.filterOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "The filter is still there");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aThirdPartyCannotChangeWhatSomebodyElsesContainerAccepts(TestContext context) {
    ServerPlayerEntity owner = filteredEast(context);

    int result =
        run(
            context,
            server(context).getCommandSource().withLevel(0),
            filterCommand(context, "add food"));

    context.assertTrue(result <= 0, "A third party wrote a filter, got " + result);
    context.assertTrue(!accepts(context, "food"), "A refused filter was written anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerThatIsNotFilteredHasNothingToList(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, filterCommand(context, ""));

    context.assertTrue(result <= 0, "A general container listed a filter, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clearingAContainerThatIsNotFilteredIsRefusedRatherThanReportedAsDone(
      TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, filterCommand(context, "clear"));

    context.assertTrue(
        result <= 0,
        "A general container accepts everything, so clearing its filter is a lie, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void addingToAContainerThatIsNotFilteredIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, filterCommand(context, "add food"));

    context.assertTrue(result <= 0, "A general container has nothing to accept, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  private boolean accepts(TestContext context, String tagId) {
    return filters.filterOf(context.getWorld(), terminal(context), east(context)).hasTag(tagId);
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

  private static String filterCommand(TestContext context, String tail) {
    return "totalstorage terminal "
        + at(terminal(context))
        + " filter "
        + at(east(context))
        + (tail.isEmpty() ? "" : " " + tail);
  }

  private static String at(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static int runAsConsole(TestContext context, String command) {
    return run(context, server(context).getCommandSource(), command);
  }

  private static int run(TestContext context, ServerCommandSource source, String command) {
    try {
      return server(context).getCommandManager().getDispatcher().execute(command, source);
    } catch (CommandSyntaxException refused) {
      return REFUSED;
    }
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
