package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Configuring one network from the command line, which is what makes a container role reachable on
 * a server with no client mod at all.
 *
 * <p>The mod's standards require every setting to be reachable from a console over SSH. Until these
 * branches existed a role was a decision the model could hold and nothing could give, so ADR 0004's
 * whole promise was unavailable from a headless server.
 *
 * <p>Permission is the point of most of what follows. An operator may configure any terminal, an
 * owner may configure their own, and a third party may configure neither, and all three are checked
 * against a real command source rather than inferred.
 */
public final class TerminalNetworkCommandGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-network-command";
  private static final int TICK_LIMIT = 60;
  private static final int REFUSED = -1;

  private final ContainerRoleService roles = new ContainerRoleService();

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

  /** Reading is open, because a player refused a container deserves to learn why. */
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
            "toomanychests terminal "
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

    int counted =
        runAsConsole(context, "toomanychests terminal " + at(terminal(context)) + " info");

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
            "toomanychests terminal "
                + at(context.getAbsolutePos(new BlockPos(6, 1, 6)))
                + " info");

    context.assertTrue(result <= 0, "A position with no terminal reported success");
    context.complete();
  }

  /** A highlight has to be shown to somebody, and the console is nobody. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCannotHighlightBecauseItIsNotLooking(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result =
        runAsConsole(context, "toomanychests terminal " + at(terminal(context)) + " highlight");

    context.assertTrue(result <= 0, "The console highlighted to nobody, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  private static String roleCommand(TestContext context, String role) {
    return "toomanychests terminal "
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
