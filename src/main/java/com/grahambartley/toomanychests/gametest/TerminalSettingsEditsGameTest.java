package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.network.ContainerRoleEditPayload;
import com.grahambartley.toomanychests.network.TerminalAccessEditPayload;
import com.grahambartley.toomanychests.network.TerminalActionPayload;
import com.grahambartley.toomanychests.network.TerminalRadiusEditPayload;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsService;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalSettingsEditsGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-settings-edits";
  private static final int TICK_LIMIT = 60;

  private final TerminalSettingsService panels = new TerminalSettingsService();
  private final TerminalSettingsEdits edits = new TerminalSettingsEdits(panels);
  private final ContainerRoleService roles = new ContainerRoleService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanMoveTheRadiusFromThePanel(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    edits.setRadius(owner, new TerminalRadiusEditPayload(syncId(owner), 3));

    context.assertTrue(
        radiusOf(context) == 3, "The radius should have moved, got " + radiusOf(context));
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotMoveTheRadiusHoweverTheyAsk(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);
    int before = radiusOf(context);

    boolean applied = edits.setRadius(guest, new TerminalRadiusEditPayload(syncId(guest), 5));

    context.assertFalse(applied, "A guest moved the radius");
    context.assertTrue(radiusOf(context) == before, "The radius changed to " + radiusOf(context));
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotLockOrUnlockTheTerminalTheyWereLetInto(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    boolean applied =
        edits.setAccessMode(
            guest, new TerminalAccessEditPayload(syncId(guest), TerminalAccessMode.OWNER));

    context.assertFalse(applied, "A guest changed the access mode");
    context.assertTrue(
        TerminalLookup.settingsAt(context.getWorld(), terminal(context)).orElseThrow().accessMode()
            == TerminalAccessMode.OPEN,
        "The access mode changed under a guest's request");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotExcludeSomebodyElsesContainer(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    boolean applied =
        edits.setRole(
            guest,
            new ContainerRoleEditPayload(syncId(guest), east(context), ContainerRole.EXCLUDED));

    context.assertFalse(applied, "A guest changed a container's role");
    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context)) == ContainerRole.GENERAL,
        "A guest's refused change was written anyway");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanExcludeAContainerFromThePanel(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    boolean applied =
        edits.setRole(
            owner,
            new ContainerRoleEditPayload(syncId(owner), east(context), ContainerRole.EXCLUDED));

    context.assertTrue(applied, "The owner could not exclude their own container");
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
  public void anEditNamingAnotherScreenIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    int before = radiusOf(context);

    boolean applied = edits.setRadius(owner, new TerminalRadiusEditPayload(syncId(owner) + 1, 5));

    context.assertFalse(applied, "A request naming another screen was honoured");
    context.assertTrue(radiusOf(context) == before, "The radius changed anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestMayStillAskWhichContainersTheTerminalHolds(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    context.assertTrue(
        edits.act(guest, new TerminalActionPayload(syncId(guest), TerminalAction.HIGHLIGHT)),
        "A guest could not ask what the terminal reaches");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotMakeSomebodyElsesTerminalWalkItsRegionAgain(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    context.assertFalse(
        edits.act(guest, new TerminalActionPayload(syncId(guest), TerminalAction.RESCAN)),
        "A guest made the terminal rescan");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRescanPicksUpWhatTheWiderRegionReaches(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.FAR_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.widenWithoutRescanning(context, 2);
    context.assertTrue(
        containerCount(context) == 1, "The network should start holding only the near barrel");

    edits.act(owner, new TerminalActionPayload(syncId(owner), TerminalAction.RESCAN));

    context.assertTrue(
        containerCount(context) == 2,
        "The rescan found " + containerCount(context) + " of the two barrels now in reach");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRescanThatFindsNothingNewLeavesTheNetworkExactlyAsItWas(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    boolean accepted =
        edits.act(owner, new TerminalActionPayload(syncId(owner), TerminalAction.RESCAN));

    context.assertTrue(accepted, "The owner was refused a rescan");
    context.assertTrue(
        containerCount(context) == 1,
        "A rescan with nothing new to find changed the network to "
            + containerCount(context)
            + " containers");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSecondRescanInTheSameTickIsRefusedAndScansNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.FAR_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    edits.act(owner, new TerminalActionPayload(syncId(owner), TerminalAction.RESCAN));
    NetworkOperationFixture.widenWithoutRescanning(context, 2);

    boolean second =
        edits.act(owner, new TerminalActionPayload(syncId(owner), TerminalAction.RESCAN));

    context.assertTrue(second == false, "A second rescan in the same tick should be refused");
    context.assertTrue(
        containerCount(context) == 1,
        "A rate limited rescan walked the region anyway and found "
            + containerCount(context)
            + " containers");
    NetworkOperationFixture.finish(context, owner);
  }

  private int containerCount(TestContext context) {
    return roles.containers(context.getWorld(), terminal(context)).size();
  }

  private static int radiusOf(TestContext context) {
    return TerminalLookup.settingsAt(context.getWorld(), terminal(context)).orElseThrow().radius();
  }

  private static int syncId(ServerPlayerEntity player) {
    return ((TerminalScreenHandler) player.currentScreenHandler).syncId;
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
