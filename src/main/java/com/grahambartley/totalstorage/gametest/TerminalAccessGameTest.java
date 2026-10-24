package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.ModBlocks;
import com.grahambartley.totalstorage.access.TerminalAccess;
import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.access.TerminalAccessService;
import com.grahambartley.totalstorage.access.TerminalTrustService;
import com.grahambartley.totalstorage.access.TerminalViewers;
import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import com.grahambartley.totalstorage.config.ConfigBuilder;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalAccessGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "terminal-access";
  private static final String CONFIG_BATCH = "server-config";
  private static final int TICK_LIMIT = 40;
  private static final int REFUSED_EXIT_CODE = -1;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos CHEST = new BlockPos(4, 1, 3);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theOwnerCanAlwaysOpenTheirOwnTerminal(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);

    context.assertTrue(
        access().open(context.getWorld(), absolute(context), owner) == TerminalAccess.OWNER,
        "The player who placed a terminal should be let in as its owner");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerIsRefusedAnOwnerLockedTerminal(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity stranger = player(context);
    ownedBy(context, owner);

    context.assertTrue(
        access().open(context.getWorld(), absolute(context), stranger) == TerminalAccess.DENIED,
        "An owner-locked terminal must refuse everybody but its owner");
    finish(context, owner, stranger);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOpenTerminalLetsAnybodyIn(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity stranger = player(context);
    ownedBy(context, owner);
    trust().setAccessMode(context.getWorld(), absolute(context), TerminalAccessMode.OPEN);

    context.assertTrue(
        access().allows(context.getWorld(), absolute(context), stranger),
        "A terminal set to open access should refuse nobody");
    finish(context, owner, stranger);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void trustingAPlayerLetsThemInStraightAway(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity friend = player(context);
    ownedBy(context, owner);
    trust().setAccessMode(context.getWorld(), absolute(context), TerminalAccessMode.TRUSTED);
    context.assertFalse(
        access().allows(context.getWorld(), absolute(context), friend),
        "A player not yet trusted should be refused");

    trust().trust(context.getWorld(), absolute(context), friend.getUuid());

    context.assertTrue(
        access().open(context.getWorld(), absolute(context), friend) == TerminalAccess.TRUSTED,
        "A player added to the trust list should be let straight in");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void untrustingAPlayerShutsThemOutStraightAway(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity friend = player(context);
    ownedBy(context, owner);
    trust().setAccessMode(context.getWorld(), absolute(context), TerminalAccessMode.TRUSTED);
    trust().trust(context.getWorld(), absolute(context), friend.getUuid());

    trust().untrust(context.getWorld(), absolute(context), friend.getUuid());

    context.assertFalse(
        access().allows(context.getWorld(), absolute(context), friend),
        "A player taken off the trust list should be refused straight away");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOperatorReachesPastAnOwnerLock(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity moderator = player(context);
    ownedBy(context, owner);
    op(context, moderator);

    TerminalAccess decision = access().open(context.getWorld(), absolute(context), moderator);

    deop(context, moderator);
    context.assertTrue(
        decision == TerminalAccess.OPERATOR,
        "An operator should reach past an owner lock as a bypass, not as an owner, but got "
            + decision);
    finish(context, owner, moderator);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPositionHoldingNoTerminalRefusesEverybody(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);

    context.setBlockState(TERMINAL, Blocks.AIR.getDefaultState());

    context.assertFalse(
        access().allows(context.getWorld(), absolute(context), owner),
        "A terminal that is no longer there must refuse even the player who owned it");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void revokingTrustClosesTheScreenTheyStillHaveOpen(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity friend = openedOn(context, CHEST);
    ownedBy(context, owner);
    trust().setAccessMode(context.getWorld(), absolute(context), TerminalAccessMode.TRUSTED);
    trust().trust(context.getWorld(), absolute(context), friend.getUuid());
    viewers().opened(address(context), friend.getUuid());
    context.assertTrue(
        friend.currentScreenHandler != friend.playerScreenHandler,
        "The test needs the revoked player to actually have a screen open");

    trust().untrust(context.getWorld(), absolute(context), friend.getUuid());

    context.assertTrue(
        friend.currentScreenHandler == friend.playerScreenHandler,
        "Losing access should close the screen the player still had open");
    context.assertFalse(
        viewers().isViewing(address(context), friend.getUuid()),
        "A player shut out should no longer be recorded as viewing the terminal");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void revokingOnePlayersTrustLeavesEveryOtherViewerAlone(TestContext context) {
    ServerPlayerEntity owner = openedOn(context, CHEST);
    ServerPlayerEntity friend = openedOn(context, CHEST);
    ownedBy(context, owner);
    trust().setAccessMode(context.getWorld(), absolute(context), TerminalAccessMode.TRUSTED);
    trust().trust(context.getWorld(), absolute(context), friend.getUuid());
    viewers().opened(address(context), owner.getUuid());
    viewers().opened(address(context), friend.getUuid());

    trust().untrust(context.getWorld(), absolute(context), friend.getUuid());

    context.assertTrue(
        owner.currentScreenHandler != owner.playerScreenHandler,
        "An owner who kept their access should keep the screen they had open");
    context.assertTrue(
        viewers().isViewing(address(context), owner.getUuid()),
        "An owner who kept their access should still be recorded as viewing");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPlayerLoggingOutStopsBeingAViewer(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);
    viewers().opened(address(context), owner.getUuid());

    viewers().closedEverything(owner.getUuid());

    context.assertFalse(
        viewers().isViewing(address(context), owner.getUuid()),
        "A player who has gone should leave no viewer entry behind them");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theOwnerCanSetTheAccessModeFromTheCommand(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);

    int result = run(context, sourceFor(context, owner), accessCommand(context, "open"));

    context.assertTrue(result == 1, "An owner should be able to open their own terminal up");
    context.assertTrue(
        terminal(context).settings().accessMode() == TerminalAccessMode.OPEN,
        "The command should have written the access mode");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangerCannotSetTheAccessModeFromTheCommand(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity stranger = player(context);
    ownedBy(context, owner);

    int result = run(context, sourceFor(context, stranger), accessCommand(context, "open"));

    context.assertTrue(result == 0, "A stranger should not be able to unlock someone else's base");
    context.assertTrue(
        terminal(context).settings().accessMode() == TerminalAccessMode.OWNER,
        "A refused change must leave the access mode exactly as it was");
    finish(context, owner, stranger);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanSetTheAccessModeOfAnyTerminal(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);

    int result =
        run(context, server(context).getCommandSource(), accessCommand(context, "trusted"));

    context.assertTrue(result == 1, "An operator moderating a terminal should be allowed");
    context.assertTrue(
        terminal(context).settings().accessMode() == TerminalAccessMode.TRUSTED,
        "The console's change should have been written");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anyoneCanReadWhichModeATerminalIsIn(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity stranger = player(context);
    ownedBy(context, owner);

    int result = run(context, sourceFor(context, stranger), accessCommand(context, null));

    context.assertTrue(result == 1, "A refused player should still be able to learn why");
    finish(context, owner, stranger);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anAccessModeThatIsNotOneIsRefused(TestContext context) {
    ServerPlayerEntity owner = player(context);
    ownedBy(context, owner);

    int result = run(context, server(context).getCommandSource(), accessCommand(context, "public"));

    context.assertTrue(result == 0, "A value that is not an access mode should be refused");
    context.assertTrue(
        terminal(context).settings().accessMode() == TerminalAccessMode.OWNER,
        "A refused value must leave the access mode alone");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CONFIG_BATCH, tickLimit = TICK_LIMIT)
  public void theServerDefaultDecidesWhatANewTerminalStartsAs(TestContext context) {
    TotalStorageConfig original = ServerConfigService.current();
    setServerDefault(context, TerminalAccessMode.OPEN);
    ServerPlayerEntity owner = player(context);

    ownedBy(context, owner);

    TerminalAccessMode placed = terminal(context).settings().accessMode();
    restore(context, original);
    context.assertTrue(
        placed == TerminalAccessMode.OPEN,
        "A server that has opened its terminals up should not need telling twice, got " + placed);
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CONFIG_BATCH, tickLimit = TICK_LIMIT)
  public void changingTheServerDefaultLeavesExistingTerminalsAlone(TestContext context) {
    TotalStorageConfig original = ServerConfigService.current();
    ServerPlayerEntity owner = player(context);
    ServerPlayerEntity stranger = player(context);
    ownedBy(context, owner);

    setServerDefault(context, TerminalAccessMode.OPEN);

    boolean strangerLetIn = access().allows(context.getWorld(), absolute(context), stranger);
    restore(context, original);
    context.assertFalse(
        strangerLetIn,
        "A terminal placed before the default moved must keep the mode it was placed with");
    finish(context, owner, stranger);
  }

  private static void ownedBy(TestContext context, ServerPlayerEntity owner) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        absolute(context),
        context.getBlockState(TERMINAL),
        owner,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
  }

  private static ServerPlayerEntity player(TestContext context) {
    return context.createMockCreativeServerPlayerInWorld();
  }

  private static void op(TestContext context, ServerPlayerEntity player) {
    server(context)
        .getPlayerManager()
        .getOpList()
        .add(
            new OperatorEntry(
                player.getGameProfile(), ServerConfigService.OP_PERMISSION_LEVEL, false));
  }

  private static void deop(TestContext context, ServerPlayerEntity player) {
    server(context).getPlayerManager().removeFromOperators(player.getGameProfile());
  }

  private static ServerPlayerEntity openedOn(TestContext context, BlockPos relative) {
    context.setBlockState(relative, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity player = player(context);
    NamedScreenHandlerFactory factory =
        context
            .getBlockState(relative)
            .createScreenHandlerFactory(context.getWorld(), context.getAbsolutePos(relative));
    player.openHandledScreen(factory);
    return player;
  }

  private static String accessCommand(TestContext context, String mode) {
    BlockPos pos = absolute(context);
    String read =
        "totalstorage terminal " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " access";
    return mode == null ? read : read + " " + mode;
  }

  private static ServerCommandSource sourceFor(TestContext context, ServerPlayerEntity player) {
    return server(context).getCommandSource().withEntity(player).withLevel(0);
  }

  private static int run(TestContext context, ServerCommandSource source, String command) {
    try {
      return server(context).getCommandManager().getDispatcher().execute(command, source);
    } catch (CommandSyntaxException ex) {
      return REFUSED_EXIT_CODE;
    }
  }

  private static void setServerDefault(TestContext context, TerminalAccessMode mode) {
    ServerConfigService.update(
        server(context),
        ConfigBuilder.from(ServerConfigService.current()).defaultTerminalAccessMode(mode).build());
  }

  private static void restore(TestContext context, TotalStorageConfig original) {
    ServerConfigService.update(server(context), original);
  }

  private static TerminalBlockEntity terminal(TestContext context) {
    return context.getBlockEntity(TERMINAL);
  }

  private static BlockPos absolute(TestContext context) {
    return context.getAbsolutePos(TERMINAL);
  }

  private static TerminalAddress address(TestContext context) {
    return TerminalAddress.of(context.getWorld(), absolute(context));
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static TerminalAccessService access() {
    return TerminalAccessControl.access();
  }

  private static TerminalTrustService trust() {
    return TerminalAccessControl.trust();
  }

  private static TerminalViewers viewers() {
    return TerminalAccessControl.viewers();
  }

  private static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      viewers().closedEverything(player.getUuid());
      player.closeHandledScreen();
      server(context).getPlayerManager().remove(player);
    }
    context.complete();
  }
}
