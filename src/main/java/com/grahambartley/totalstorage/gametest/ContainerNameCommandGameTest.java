package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.terminal.ContainerNamingService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerNameCommandGameTest implements FabricGameTest {
  private static final String BATCH = "container-name-command";
  private static final int TICK_LIMIT = 60;
  private static final int REFUSED = -1;

  private final ContainerNamingService names = new ContainerNamingService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theConsoleCanNameAContainer(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, nameCommand(context, "set Potions and dust"));

    context.assertTrue(result > 0, "The console could not name a container, got " + result);
    context.assertTrue(
        "Potions and dust".equals(nameOf(context)),
        "The name did not stick, got '" + nameOf(context) + "'");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clearingTakesTheNameBackOff(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    runAsConsole(context, nameCommand(context, "set Potions"));

    int result = runAsConsole(context, nameCommand(context, "clear"));

    context.assertTrue(result > 0, "Clearing was refused, got " + result);
    context.assertTrue(nameOf(context).isEmpty(), "The name is still there");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNameWithNothingReadableInItIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result = runAsConsole(context, nameCommand(context, "set    "));

    context.assertTrue(result <= 0, "An unreadable name was accepted, got " + result);
    context.assertTrue(nameOf(context).isEmpty(), "An unreadable name was written anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aThirdPartyCannotNameSomebodyElsesContainer(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int result =
        run(
            context,
            server(context).getCommandSource().withLevel(0),
            nameCommand(context, "set Mine now"));

    context.assertTrue(result <= 0, "A third party named it, got " + result);
    context.assertTrue(nameOf(context).isEmpty(), "A refused name was written anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anyoneCanReadWhatAContainerIsCalled(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    runAsConsole(context, nameCommand(context, "set Potions"));

    int result =
        run(context, server(context).getCommandSource().withLevel(0), nameCommand(context, ""));

    context.assertTrue(result > 0, "Reading a name was refused, got " + result);
    NetworkOperationFixture.finish(context, owner);
  }

  private String nameOf(TestContext context) {
    return names.nameOf(context.getWorld(), terminal(context), east(context));
  }

  private static String nameCommand(TestContext context, String tail) {
    return "totalstorage terminal "
        + at(terminal(context))
        + " name "
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
