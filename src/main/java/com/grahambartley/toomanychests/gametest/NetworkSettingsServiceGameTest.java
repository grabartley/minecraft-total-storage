package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.NetworkSettingsService;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * What the settings panel is told about a terminal.
 *
 * <p>Everything here is decided on the server because the client holds none of it: the radius cap
 * lives in the server config, the roles live on a block entity, and a trust list of UUIDs can only
 * be turned into names by the server that has seen those players. What a world proves is that each
 * of those really reaches the panel rather than being defaulted quietly.
 */
public final class NetworkSettingsServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-settings";
  private static final int TICK_LIMIT = 60;

  private final NetworkSettingsService settings = new NetworkSettingsService();
  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void thePanelDescribesTheTerminalAsItStands(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 5);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertTrue(view.powered(), "A running terminal should report as powered");
    context.assertTrue(view.containerTotal() == 2, "Counted " + view.containerTotal());
    context.assertTrue(view.containers().size() == 2, "Sent " + view.containers().size() + " rows");
    context.assertTrue(view.tally().general() == 2, "Both barrels should start general");
    NetworkOperationFixture.finish(context, owner);
  }

  /** ADR 0008: the flag decides what is drawn, and the owner is the one it is true for. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void onlyTheOwnerIsToldTheyMayConfigureIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    context.assertTrue(
        viewFor(owner, context, 0, 8).mayAdminister(), "The owner should be able to configure it");
    context.assertFalse(
        viewFor(guest, context, 0, 8).mayAdminister(),
        "A guest should not be able to configure it");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  /** A trust list names players who are not the one reading it, so a guest is sent none of it. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestIsNotSentTheTrustList(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);
    TerminalAccessControl.trust().trust(context.getWorld(), terminal(context), guest.getUuid());

    context.assertTrue(
        viewFor(owner, context, 0, 8).trusted().size() == 1,
        "The owner should be shown who they trust");
    context.assertTrue(
        viewFor(guest, context, 0, 8).trusted().isEmpty(),
        "A guest was shown somebody else's trust list");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theRoleAContainerWasGivenReachesThePanel(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(owner),
        east(context),
        ContainerRole.EXCLUDED);

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertTrue(view.tally().excluded() == 1, "The tally missed the excluded container");
    context.assertTrue(
        view.containers().get(0).role() == ContainerRole.EXCLUDED,
        "The row missed the excluded role");
    NetworkOperationFixture.finish(context, owner);
  }

  /** A page is a slice of the list, so a panel scrolled down is sent the rows it is drawing. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWindowIsAnsweredWithOnlyTheRowsItCovers(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkSettingsView view = viewFor(owner, context, 1, 1);

    context.assertTrue(view.containers().size() == 1, "Sent " + view.containers().size() + " rows");
    context.assertTrue(view.containerOffset() == 1, "The page should say where it starts");
    context.assertTrue(
        view.containerTotal() == 2, "A window narrows the rows without narrowing the total");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalSaysSoRatherThanLookingEmpty(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.AIR);
    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertFalse(view.powered(), "An unpowered terminal reported as powered");
    context.assertFalse(view.hasContainers(), "An unpowered terminal listed containers");
    NetworkOperationFixture.finish(context, owner);
  }

  /** The access mode the panel shows has to be the one the terminal is really in. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theAccessModeShownIsTheOneTheTerminalIsIn(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TerminalAccessControl.trust()
        .setAccessMode(context.getWorld(), terminal(context), TerminalAccessMode.TRUSTED);

    context.assertTrue(
        viewFor(owner, context, 0, 8).accessMode() == TerminalAccessMode.TRUSTED,
        "The panel showed the wrong access mode");
    NetworkOperationFixture.finish(context, owner);
  }

  private NetworkSettingsView viewFor(
      ServerPlayerEntity player, TestContext context, int offset, int length) {
    return settings.viewFor(player, terminal(context), new ListingWindow(offset, length));
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
