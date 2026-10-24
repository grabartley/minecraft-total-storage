package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalNetworkCommandGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-network-command";
  private static final int TICK_LIMIT = 60;
  private static final int REFUSED = -1;

  private final ContainerRoleService roles = new ContainerRoleService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanExcludeAContainer(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, roleCommand(context, "excluded"));

    context.assertTrue(result > 0, "The console could not exclude a container");
    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context))
            == ContainerRole.EXCLUDED,
        "The role did not stick");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoOwnsNoTerminalCannotChangeItsRoles(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result =
        run(
            context,
            server(context).getCommandSource().withLevel(0),
            roleCommand(context, "excluded"));

    context.assertTrue(result <= 0, "A third party changed a role, got " + result);
    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context)) == ContainerRole.GENERAL,
        "A refused change was written anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anyoneCanReadWhatRoleAContainerHolds(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result =
        run(context, server(context).getCommandSource().withLevel(0), roleCommand(context, ""));

    context.assertTrue(result > 0, "A plain player could not read a role, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerTheTerminalCannotReachIsRefusedFromTheConsole(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    BlockPos distant = context.getAbsolutePos(new BlockPos(6, 1, 6));

    int result =
        runAsConsole(
            context,
            "totalstorage terminal "
                + at(terminal(context))
                + " role "
                + at(distant)
                + " excluded");

    context.assertTrue(result <= 0, "A container out of range was configured, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanSummariseATerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int counted = runAsConsole(context, "totalstorage terminal " + at(terminal(context)) + " info");

    context.assertTrue(counted == 2, "The summary counted " + counted + " of two containers");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPositionHoldingNoTerminalIsReportedRatherThanFailingObscurely(TestContext context) {
    int result =
        runAsConsole(
            context,
            "totalstorage terminal " + at(context.getAbsolutePos(new BlockPos(6, 1, 6))) + " info");

    context.assertTrue(result <= 0, "A position with no terminal reported success");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoOwnsNoTerminalCannotMakeItWalkItsRegionAgain(TestContext context) {
    ServerPlayerEntity owner = openTerminalWithAWiderRegionToFind(context);

    int result =
        run(context, server(context).getCommandSource().withLevel(0), rescanCommand(context));

    context.assertTrue(result == 0, "A third party's rescan reported work, got " + result);
    context.assertTrue(
        containerCount(context) == 1,
        "A third party made the terminal pick up " + containerCount(context) + " containers");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanMakeATerminalWalkItsRegionAgain(TestContext context) {
    ServerPlayerEntity owner = openTerminalWithAWiderRegionToFind(context);

    int result = runAsConsole(context, rescanCommand(context));

    context.assertTrue(result > 0, "The console was refused a rescan, got " + result);
    context.assertTrue(
        containerCount(context) == 2,
        "The rescan found " + containerCount(context) + " of the two barrels now in reach");
    NetworkOperationFixture.finish(context, owner);
  }

  private static ServerPlayerEntity openTerminalWithAWiderRegionToFind(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.FAR_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.widenWithoutRescanning(context, 2);
    return owner;
  }

  private int containerCount(TestContext context) {
    return roles.containers(context.getWorld(), terminal(context)).size();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCannotHighlightBecauseItIsNotLooking(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, highlightCommand(context));

    context.assertTrue(result <= 0, "The console highlighted to nobody, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanSetTheRadiusOfTheirOwnTerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = run(context, asPlayer(context, owner), radiusCommand(context, 2));

    context.assertTrue(result == 2, "An owner was refused their own terminal, got " + result);
    context.assertTrue(radiusOf(context) == 2, "The owner's radius was not written");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aThirdPartyCannotSetTheRadiusOfSomebodyElsesTerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity stranger = NetworkOperationFixture.standingPlayer(context);

    int result = run(context, asPlayer(context, stranger), radiusCommand(context, 2));

    context.assertTrue(result == 0, "A third party moved a radius, got " + result);
    context.assertTrue(radiusOf(context) == 1, "A refused set wrote the radius anyway");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanSetTheRadiusOfATerminalItDoesNotOwn(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, radiusCommand(context, 2));

    context.assertTrue(result == 2, "The console was refused a radius, got " + result);
    context.assertTrue(radiusOf(context) == 2, "The console's radius was not written");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoCannotOpenATerminalCannotHighlightItsNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity stranger = NetworkOperationFixture.standingPlayer(context);

    int result = run(context, asPlayer(context, stranger), highlightCommand(context));

    context.assertTrue(
        result == 0, "A third party was shown somebody else's containers, got " + result);
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoMayOpenATerminalCanStillHighlightItsNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = run(context, asPlayer(context, owner), highlightCommand(context));

    context.assertTrue(result == 1, "The owner was shown " + result + " of one container");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRescanFindsAContainerPlacedSinceTheLastScanWithTheRegionUnchanged(
      TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    context.assertTrue(
        containerCount(context) == 1,
        "A barrel placed after the scan should not be in the network");

    int result = run(context, asPlayer(context, owner), rescanCommand(context));

    context.assertTrue(result > 0, "An owner was refused a rescan, got " + result);
    context.assertTrue(
        containerCount(context) == 2,
        "The rescan held " + containerCount(context) + " of the two barrels in reach");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSecondRescanFromTheCommandLineInTheSameTickIsRefusedAndScansNothing(
      TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    int first = run(context, asPlayer(context, owner), rescanCommand(context));
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);

    int second = run(context, asPlayer(context, owner), rescanCommand(context));

    context.assertTrue(first > 0, "The owner's first rescan was refused, got " + first);
    context.assertTrue(second == 0, "A second rescan in the same tick should be refused");
    context.assertTrue(
        containerCount(context) == 1,
        "A rate limited rescan walked the region anyway and held "
            + containerCount(context)
            + " containers");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRescanWithNoRunningNetworkIsReportedWithoutSpendingTheLimit(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));

    int whileOff = run(context, asPlayer(context, owner), rescanCommand(context));
    TerminalNetworks.power()
        .loaded(
            context.getWorld(),
            terminal(context),
            context.getBlockState(NetworkOperationFixture.TERMINAL));
    int onceOnAgain = run(context, asPlayer(context, owner), rescanCommand(context));

    context.assertTrue(
        whileOff == 0, "A terminal with no network reported a scan, got " + whileOff);
    context.assertTrue(
        onceOnAgain > 0,
        "The refused rescan spent the limit that the next one needed, got " + onceOnAgain);
    NetworkOperationFixture.finish(context, owner);
  }

  private static int radiusOf(TestContext context) {
    return TerminalLookup.settingsAt(context.getWorld(), terminal(context)).orElseThrow().radius();
  }

  private static ServerCommandSource asPlayer(TestContext context, ServerPlayerEntity player) {
    return server(context).getCommandSource().withLevel(0).withEntity(player);
  }

  private static String radiusCommand(TestContext context, int blocks) {
    return "totalstorage terminal " + at(terminal(context)) + " radius " + blocks;
  }

  private static String rescanCommand(TestContext context) {
    return "totalstorage terminal " + at(terminal(context)) + " rescan";
  }

  private static String highlightCommand(TestContext context) {
    return "totalstorage terminal " + at(terminal(context)) + " highlight";
  }

  private static String roleCommand(TestContext context, String role) {
    return "totalstorage terminal "
        + at(terminal(context))
        + " role "
        + at(east(context))
        + (role.isEmpty() ? "" : " " + role);
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
