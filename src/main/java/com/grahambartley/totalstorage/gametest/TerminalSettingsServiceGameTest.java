package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.network.NetworkSettingsRequestPayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

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
        panels.request(player, NetworkSettingsRequestPayload.showing(handler.syncId, 0, 4));

    context.assertTrue(answered, "A request from an open screen should be answered");
    context.assertTrue(handler.settings().isOpen(), "The panel should be recorded as open");
    context.assertTrue(
        handler.settings().window().length() == 4,
        "The window should be what was asked for, got " + handler.settings().window());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRequestNamingAnotherScreenIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);

    boolean answered =
        panels.request(player, NetworkSettingsRequestPayload.showing(handler.syncId + 1, 0, 4));

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
    panels.request(player, NetworkSettingsRequestPayload.showing(handler.syncId, 0, 4));

    context.assertTrue(panels.refresh(player), "An open panel should be pushed to");
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
    panels.request(player, NetworkSettingsRequestPayload.showing(handler.syncId, 0, 4));

    context.assertTrue(
        panels.viewFor(player, handler).containerTotal() == 1,
        "The panel should describe the one barrel in range");
    context.assertTrue(
        panels.viewFor(player, handler).mayAdminister(),
        "The owner should be told they may configure it");
    NetworkOperationFixture.finish(context, player);
  }
}
