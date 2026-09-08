package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.network.NetworkSettingsRequestPayload;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsService;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * What a settings panel is answered with when it asks, and when it is left alone.
 *
 * <p>The same contract the item listing keeps: a request naming a screen the player does not have
 * open reaches nothing, and a panel that has asked for nothing is never pushed to. The second half
 * matters more here than it does for the listing, because a player on the item grid is still
 * holding a handler that could be sent settings it is not drawing.
 */
public final class TerminalSettingsServiceGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-settings-service";
  private static final int TICK_LIMIT = 60;

  private final TerminalSettingsService panels = new TerminalSettingsService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRequestIsRecordedAsWhatThePanelIsShowing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);

    boolean answered =
        panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));

    context.assertTrue(answered, "A request from an open screen should be answered");
    context.assertTrue(handler.settings().isOpen(), "The panel should be recorded as open");
    context.assertTrue(
        handler.settings().window().length() == 4,
        "The window should be what was asked for, got " + handler.settings().window());
    NetworkOperationFixture.finish(context, player);
  }

  /** Per ADR 0009 the sync id is checked against the screen the server holds, never trusted. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRequestNamingAnotherScreenIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);

    boolean answered =
        panels.request(player, new NetworkSettingsRequestPayload(handler.syncId + 1, 0, 4));

    context.assertFalse(answered, "A request naming another screen was answered");
    context.assertFalse(handler.settings().isOpen(), "A refused request opened the panel anyway");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aScreenShowingTheGridIsNotPushedSettings(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    context.assertFalse(
        panels.refresh(player), "A screen that never opened the panel was pushed to");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aScreenShowingThePanelIsPushedToWhenSomethingChanges(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));

    context.assertTrue(panels.refresh(player), "An open panel should be pushed to");
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * Going back to the grid closes the panel, so an edit by somebody else stops reaching a screen
   * that is not drawing it. A window of no rows is what says so.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void askingForNoRowsClosesThePanelAgain(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));

    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 0));

    context.assertFalse(handler.settings().isOpen(), "The panel stayed open");
    context.assertFalse(panels.refresh(player), "A closed panel was pushed to");
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * The case the close path skips the gate for. A terminal that lost power under an open panel is
   * exactly when the server must stop pushing to it, and the gate needs a live network.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPanelClosesEvenWhenTheNetworkHasGone(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.AIR);
    TerminalNetworks.power()
        .unloaded(context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));

    boolean closed =
        panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 0));

    context.assertTrue(closed, "An unpowered terminal refused to close its panel");
    context.assertFalse(handler.settings().isOpen(), "The panel stayed open");
    NetworkOperationFixture.finish(context, player);
  }

  /** A close naming a screen the sender does not have open reaches nothing, like every request. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aCloseNamingAnotherScreenIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));

    boolean closed =
        panels.request(player, new NetworkSettingsRequestPayload(handler.syncId + 1, 0, 0));

    context.assertFalse(closed, "A close naming another screen was honoured");
    context.assertTrue(handler.settings().isOpen(), "Somebody else's close shut this panel");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void thePanelBuiltForAScreenDescribesTheTerminalItIsOpenOn(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    panels.request(player, new NetworkSettingsRequestPayload(handler.syncId, 0, 4));

    context.assertTrue(
        panels.viewFor(player, handler).containerTotal() == 1,
        "The panel should describe the one barrel in range");
    context.assertTrue(
        panels.viewFor(player, handler).mayAdminister(),
        "The owner should be told they may configure it");
    NetworkOperationFixture.finish(context, player);
  }
}
