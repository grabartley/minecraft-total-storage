package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.RoleChangeOutcome;
import com.grahambartley.totalstorage.terminal.RoleTally;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerRoleServiceGameTest implements FabricGameTest {
  private static final String BATCH = "container-roles";
  private static final int TICK_LIMIT = 60;

  private static final BlockPos DISTANT = new BlockPos(6, 1, 6);

  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerNobodyHasConfiguredStartsGeneral(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context)) == ContainerRole.GENERAL,
        "A container starts as one the network may use");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanExcludeAContainerAndItSticks(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        roles.assign(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(player),
            east(context),
            ContainerRole.EXCLUDED);

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "The role should have been written");
    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context))
            == ContainerRole.EXCLUDED,
        "The role should have stuck");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void givingAContainerTheRoleItAlreadyHeldWritesNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        roles.assign(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(player),
            east(context),
            ContainerRole.GENERAL);

    context.assertTrue(
        outcome == RoleChangeOutcome.UNCHANGED, "An unchanged role should say so, got " + outcome);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerTheTerminalCannotReachIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, DISTANT);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome =
        roles.assign(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(player),
            context.getAbsolutePos(DISTANT),
            ContainerRole.EXCLUDED);

    context.assertTrue(
        outcome == RoleChangeOutcome.NOT_IN_NETWORK,
        "A container out of range should be refused, got " + outcome);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoIsNotTheOwnerCannotChangeARole(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    RoleChangeOutcome outcome =
        roles.assign(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(guest),
            east(context),
            ContainerRole.EXCLUDED);

    context.assertTrue(
        outcome == RoleChangeOutcome.DENIED, "A guest changed a role, got " + outcome);
    context.assertTrue(
        roles.roleOf(context.getWorld(), terminal(context), east(context)) == ContainerRole.GENERAL,
        "A guest's refused change was written anyway");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyContainerTheTerminalHoldsIsListedWithItsRole(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 12);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(player),
        east(context),
        ContainerRole.FILTERED);

    List<ContainerRoleEntry> listed = roles.containers(context.getWorld(), terminal(context));

    context.assertTrue(listed.size() == 2, "Listed " + listed.size() + " of two containers");
    ContainerRoleEntry stocked =
        listed.stream()
            .filter(row -> row.container().equals(east(context)))
            .findFirst()
            .orElseThrow();
    context.assertTrue(
        stocked.role() == ContainerRole.FILTERED, "The listed role should be the stored one");
    context.assertTrue(
        stocked.variantCount() == 1 && stocked.itemCount() == 12L,
        "The row should say what is in the container, got "
            + stocked.variantCount()
            + " kinds and "
            + stocked.itemCount());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRowLeadsWithWhateverTheContainerHoldsMostOf(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 50);
    barrel.setStack(1, new ItemStack(Items.COBBLESTONE, 30));
    barrel.setStack(2, new ItemStack(Items.COBBLESTONE, 30));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    ContainerRoleEntry row =
        roles.containers(context.getWorld(), terminal(context)).stream()
            .filter(listed -> listed.container().equals(east(context)))
            .findFirst()
            .orElseThrow();

    context.assertTrue(
        row.icon().map(icon -> icon.isOf(Items.COBBLESTONE)).orElse(false),
        "The icon should be what there is most of, got " + row.icon());
    context.assertTrue(
        row.variantCount() == 2,
        "Three stacks of two kinds should count two, got " + row.variantCount());
    context.assertTrue(row.itemCount() == 110L, "Counted " + row.itemCount() + " of 110 items");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theTallyCountsWhatTheNetworkMayAndMayNotWriteTo(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(player),
        east(context),
        ContainerRole.EXCLUDED);

    RoleTally tally = roles.tally(context.getWorld(), terminal(context));

    context.assertTrue(tally.total() == 2, "Counted " + tally.total() + " of two containers");
    context.assertTrue(tally.excluded() == 1, "Counted " + tally.excluded() + " excluded");
    context.assertTrue(tally.general() == 1, "Counted " + tally.general() + " general");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalListsNoContainers(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));

    context.assertTrue(
        roles.containers(context.getWorld(), terminal(context)).isEmpty(),
        "An unpowered terminal listed containers");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRowSaysHowManyOfItsSlotsAreInUse(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 12);
    barrel.setStack(1, new ItemStack(Items.COBBLESTONE, 4));
    barrel.markDirty();
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    List<ContainerRoleEntry> listed = roles.containers(context.getWorld(), terminal(context));
    ContainerRoleEntry stocked = rowFor(listed, east(context));
    ContainerRoleEntry empty =
        rowFor(listed, context.getAbsolutePos(NetworkOperationFixture.WEST_BARREL));

    context.assertTrue(
        stocked.usedSlots() == 2 && stocked.totalSlots() == barrel.size(),
        "A barrel holding two stacks should report 2 of "
            + barrel.size()
            + " but reported "
            + stocked.usedSlots()
            + " of "
            + stocked.totalSlots());
    context.assertTrue(
        empty.usedSlots() == 0 && empty.totalSlots() == barrel.size(),
        "An empty barrel should report 0 of "
            + barrel.size()
            + " but reported "
            + empty.usedSlots()
            + " of "
            + empty.totalSlots());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theRowsAddUpToTheCapacityTheTerminalSectionShows(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 12);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(player),
        east(context),
        ContainerRole.EXCLUDED);

    List<ContainerRoleEntry> listed = roles.containers(context.getWorld(), terminal(context));
    NetworkCapacity capacity = NetworkCapacity.of(listed);
    ContainerRoleEntry counted =
        rowFor(listed, context.getAbsolutePos(NetworkOperationFixture.WEST_BARREL));

    context.assertTrue(
        capacity.totalSlots() == counted.totalSlots(),
        "An excluded container should be left out of the capacity, got " + capacity);
    context.assertTrue(
        capacity.usedSlots() == counted.usedSlots(),
        "The capacity should be the sum of the rows it counts, got " + capacity);
    NetworkOperationFixture.finish(context, player);
  }

  private static ContainerRoleEntry rowFor(List<ContainerRoleEntry> rows, BlockPos container) {
    return rows.stream().filter(row -> row.container().equals(container)).findFirst().orElseThrow();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
