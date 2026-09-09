package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ConfigBuilder;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalRadiusService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalRadiusGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "server-config";
  private static final int TICK_LIMIT = 40;
  private static final int REFUSED_EXIT_CODE = -1;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos NEAR_BARREL = new BlockPos(4, 1, 3);
  private static final BlockPos FAR_BARREL = new BlockPos(5, 1, 3);

  private static final TerminalRadiusService RADIUS =
      new TerminalRadiusService(TerminalNetworks.power());

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTerminalReachesOnlyAsFarAsItsRadius(TestContext context) {
    stock(context, NEAR_BARREL, new ItemStack(Items.DIAMOND, 8));
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 1);

    switchOn(context);

    context.assertTrue(
        diamonds(context) == 8L, "A barrel one block away should be inside a radius of one");
    context.assertTrue(
        emeralds(context) == 0L, "A barrel two blocks away should be outside a radius of one");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void raisingTheRadiusBringsFurtherContainersIn(TestContext context) {
    stock(context, NEAR_BARREL, new ItemStack(Items.DIAMOND, 8));
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 1);
    switchOn(context);

    setRadius(context, 2);

    context.assertTrue(
        emeralds(context) == 5L,
        "Raising the radius should reach the barrel it could not before, listed "
            + emeralds(context));
    context.assertTrue(
        diamonds(context) == 8L, "Raising the radius should keep what it already had");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loweringTheRadiusDropsTheContainersItNoLongerReaches(TestContext context) {
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 2);
    switchOn(context);
    context.assertTrue(emeralds(context) == 5L, "The terminal should have reached the far barrel");

    setRadius(context, 1);

    context.assertTrue(
        emeralds(context) == 0L,
        "A terminal that no longer reaches a barrel must not still be offering what is in it");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void changingTheRadiusCostsExactlyOneScan(TestContext context) {
    stock(context, NEAR_BARREL, new ItemStack(Items.DIAMOND, 8));
    placeTerminal(context, 1);
    switchOn(context);
    NetworkMembership before = network(context);

    setRadius(context, 2);
    NetworkMembership afterFirstChange = network(context);
    setRadius(context, 2);
    NetworkMembership afterSecondChange = network(context);

    context.assertTrue(
        afterFirstChange != before, "Changing the radius should scan the new volume once");
    context.assertTrue(
        afterSecondChange == afterFirstChange,
        "Setting the radius it already has should not scan again");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRadiusAboveTheServerMaximumIsClampedOnWrite(TestContext context) {
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 1);
    switchOn(context);
    TooManyChestsConfig original = ServerConfigService.current();
    setServerMaximum(context, 1);

    Optional<TerminalRadius> applied = RADIUS.apply(context.getWorld(), absolute(context), 5);

    boolean clamped = applied.isPresent() && applied.get().clamped();
    int stored = terminal(context).settings().radius();
    long stillOutOfReach = emeralds(context);
    restore(context, original);

    context.assertTrue(clamped, "A radius above the server maximum should report that it clamped");
    context.assertTrue(
        applied.get().blocks() == 1,
        "The clamp should land on the server maximum but landed on " + applied.get().blocks());
    context.assertTrue(
        stored == 1, "The terminal should have been written the clamped radius, not " + stored);
    context.assertTrue(
        stillOutOfReach == 0L, "A clamped terminal must not reach what the clamp excludes");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loweringTheServerMaximumShrinksATerminalAlreadyRunning(TestContext context) {
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 2);
    switchOn(context);
    context.assertTrue(emeralds(context) == 5L, "The terminal should have reached the far barrel");
    TooManyChestsConfig original = ServerConfigService.current();

    setServerMaximum(context, 1);

    long reachAfterClamp = emeralds(context);
    int runningRadius = network(context).region().radius();
    restore(context, original);

    context.assertTrue(
        reachAfterClamp == 0L,
        "Lowering the maximum should leave no terminal reaching further than it");
    context.assertTrue(
        runningRadius == 1, "The running terminal should walk a radius of 1, not " + runningRadius);
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void raisingTheServerMaximumAgainGivesTheReachBack(TestContext context) {
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 2);
    switchOn(context);
    TooManyChestsConfig original = ServerConfigService.current();

    setServerMaximum(context, 1);
    int askedForWhileClamped = terminal(context).settings().radius();
    restore(context, original);

    context.assertTrue(
        askedForWhileClamped == 2,
        "A clamped terminal should still be asking for 2 but asks for " + askedForWhileClamped);
    context.assertTrue(
        emeralds(context) == 5L, "Restoring the maximum should reach the far barrel again");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanSetOneTerminalsRadius(TestContext context) {
    stock(context, FAR_BARREL, new ItemStack(Items.EMERALD, 5));
    placeTerminal(context, 1);
    switchOn(context);

    int result = runAsConsole(context, radiusCommand(context, 2));

    context.assertTrue(result > 0, "Setting a terminal's radius should report success");
    context.assertTrue(
        terminal(context).settings().radius() == 2, "The command should have written the radius");
    context.assertTrue(
        emeralds(context) == 5L, "The command should have rescanned the larger volume");
    tearDown(context);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anyoneCanReadOneTerminalsRadius(TestContext context) {
    placeTerminal(context, 2);

    int result = runAsPlayer(context, readRadiusCommand(context));

    context.assertTrue(result == 2, "Reading a terminal's radius should be open to anyone");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNonOperatorCannotSetOneTerminalsRadius(TestContext context) {
    placeTerminal(context, 1);

    int result = runAsPlayer(context, radiusCommand(context, 2));

    context.assertTrue(result == REFUSED_EXIT_CODE, "A non-operator should not reach radius set");
    context.assertTrue(
        terminal(context).settings().radius() == 1, "A refused set should leave the radius alone");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPositionHoldingNoTerminalIsRefused(TestContext context) {
    BlockPos barrel = context.getAbsolutePos(NEAR_BARREL);
    context.setBlockState(NEAR_BARREL, Blocks.BARREL.getDefaultState());

    int result =
        runAsConsole(
            context,
            "toomanychests terminal "
                + barrel.getX()
                + " "
                + barrel.getY()
                + " "
                + barrel.getZ()
                + " radius 4");

    context.assertTrue(result == 0, "A block that is not a terminal should report failure");
    context.complete();
  }

  private static void placeTerminal(TestContext context, int radius) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    terminal(context).updateSettings(settings -> settings.withRadius(radius));
  }

  private static void setRadius(TestContext context, int radius) {
    RADIUS.apply(context.getWorld(), absolute(context), radius);
  }

  private static void setServerMaximum(TestContext context, int maximum) {
    ServerConfigService.update(
        server(context),
        ConfigBuilder.from(ServerConfigService.current()).maxTerminalRadius(maximum).build());
  }

  private static void restore(TestContext context, TooManyChestsConfig original) {
    ServerConfigService.update(server(context), original);
  }

  private static void switchOn(TestContext context) {
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
  }

  private static TerminalBlockEntity terminal(TestContext context) {
    return context.getBlockEntity(TERMINAL);
  }

  private static BlockPos absolute(TestContext context) {
    return context.getAbsolutePos(TERMINAL);
  }

  private static NetworkMembership network(TestContext context) {
    return TerminalNetworks.power().network(context.getWorld(), absolute(context)).orElse(null);
  }

  private static long diamonds(TestContext context) {
    return network(context).index().countOf(ItemVariant.of(Items.DIAMOND));
  }

  private static long emeralds(TestContext context) {
    return network(context).index().countOf(ItemVariant.of(Items.EMERALD));
  }

  private static void stock(TestContext context, BlockPos pos, ItemStack... stacks) {
    context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(pos);
    for (int slot = 0; slot < stacks.length; slot++) {
      barrel.setStack(slot, stacks[slot]);
    }
  }

  private static String radiusCommand(TestContext context, int radius) {
    return readRadiusCommand(context) + " " + radius;
  }

  private static String readRadiusCommand(TestContext context) {
    BlockPos pos = absolute(context);
    return "toomanychests terminal " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " radius";
  }

  private static int runAsConsole(TestContext context, String command) {
    return run(context, server(context).getCommandSource(), command);
  }

  private static int runAsPlayer(TestContext context, String command) {
    return run(context, server(context).getCommandSource().withLevel(0), command);
  }

  private static int run(TestContext context, ServerCommandSource source, String command) {
    try {
      return server(context).getCommandManager().getDispatcher().execute(command, source);
    } catch (CommandSyntaxException ex) {
      return REFUSED_EXIT_CODE;
    }
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static void tearDown(TestContext context) {
    TerminalNetworks.power().unloaded(context.getWorld(), absolute(context));
  }
}
