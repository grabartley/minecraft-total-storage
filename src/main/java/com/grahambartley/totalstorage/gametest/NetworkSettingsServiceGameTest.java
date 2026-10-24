package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.NetworkSettingsService;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class NetworkSettingsServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-settings";

  private static final int BARREL_SLOTS = 27;

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
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void thePanelReportsTheRoomTheNetworkReallyHas(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 5);
    barrel.setStack(1, new ItemStack(Items.COBBLESTONE, 9));
    barrel.markDirty();
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertTrue(
        view.capacity().totalSlots() == BARREL_SLOTS,
        "A barrel should report "
            + BARREL_SLOTS
            + " slots, reported "
            + view.capacity().totalSlots());
    context.assertTrue(
        view.capacity().usedSlots() == 2,
        "Two filled slots should read as two, read " + view.capacity().usedSlots());
    context.assertTrue(
        view.capacity().freeSlots() == BARREL_SLOTS - 2,
        "Free should be what is left, read " + view.capacity().freeSlots());
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsNeitherRoomNorFullness(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertTrue(
        view.capacity().totalSlots() == BARREL_SLOTS,
        "Only the writable barrel is room, counted " + view.capacity().totalSlots());
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkReachingNothingReportsNoCapacityRatherThanBeingFull(TestContext context) {
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkSettingsView view = viewFor(owner, context, 0, 8);

    context.assertTrue(view.capacity().isUnknown(), "A network with no containers knows no room");
    context.assertTrue(!view.capacity().isFull(), "Reaching nothing is not being full");
    NetworkOperationFixture.finish(context, owner);
  }

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

    context.assertTrue(
        view.containers().get(0).role() == ContainerRole.EXCLUDED,
        "The row missed the excluded role");
    NetworkOperationFixture.finish(context, owner);
  }

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
