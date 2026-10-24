package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.operation.DepositOutcome;
import com.grahambartley.totalstorage.operation.DepositResult;
import com.grahambartley.totalstorage.operation.DepositService;
import com.grahambartley.totalstorage.operation.OperationMessages;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public final class DepositLeftoverReportGameTest implements FabricGameTest {
  private static final String BATCH = "deposit-leftover-report";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private final DepositService deposits = new DepositService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPartialDepositAccountsForWhatStayedBehindByItem(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(0, new ItemStack(Items.COBBLESTONE, 40));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 30));
    player.getInventory().setStack(1, new ItemStack(Items.COBBLESTONE, 30));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.PARTIAL,
        "Room for some of it should report a partial deposit, got " + outcome.result());
    context.assertTrue(
        outcome.leftoverKinds() == 1,
        "Two slots of one kind should report one kind, reported " + outcome.leftoverKinds());
    context.assertTrue(
        outcome.leftovers().get(0).variant().isOf(Items.COBBLESTONE),
        "The leftover should be named, got " + outcome.leftovers().get(0).variant());
    context.assertTrue(
        outcome.leftovers().get(0).amount()
            == NetworkOperationFixture.countCarried(player, Items.COBBLESTONE),
        "Reported "
            + outcome.leftovers().get(0).amount()
            + " left over but the player holds "
            + NetworkOperationFixture.countCarried(player, Items.COBBLESTONE));
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWholeDepositReportsNoLeftoversAtAll(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.DEPOSITED,
        "An empty barrel should take the lot, got " + outcome.result());
    context.assertTrue(
        outcome.leftoverKinds() == 0,
        "A whole deposit reported " + outcome.leftoverKinds() + " kinds left behind");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void thePartialLineNamesTheItemThatStayedBehind(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(0, new ItemStack(Items.COBBLESTONE, 54));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 22));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    Text spoken = OperationMessages.forDeposit(outcome).orElseThrow();
    String line = spoken.getString();

    context.assertTrue(
        outcome.result() == DepositResult.PARTIAL,
        "This should be a partial deposit, got " + outcome.result());
    context.assertTrue(
        ((TranslatableTextContent) spoken.getContent())
            .getKey()
            .equals("operation.total-storage.deposit.partial"),
        "A partial deposit borrowed another line, got "
            + ((TranslatableTextContent) spoken.getContent()).getKey());
    context.assertTrue(
        line.contains(Items.COBBLESTONE.getName().getString()),
        "The line should name what stayed behind, got \"" + line + "\"");
    context.assertTrue(
        line.contains(Long.toString(outcome.remaining())),
        "The line should count it, got \"" + line + "\"");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aLineWithMoreKindsThanItNamesCountsTheRest(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(0, new ItemStack(Items.COBBLESTONE, 60));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 40));
    player.getInventory().setStack(1, new ItemStack(Items.DIAMOND, 30));
    player.getInventory().setStack(2, new ItemStack(Items.OAK_LOG, 20));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    String line = OperationMessages.forDeposit(outcome).map(Text::getString).orElse("");

    context.assertTrue(
        outcome.leftoverKinds() == 3,
        "Three kinds should stay behind, reported " + outcome.leftoverKinds());
    context.assertTrue(
        line.contains(Items.COBBLESTONE.getName().getString()),
        "The largest leftover should be named, got \"" + line + "\"");
    context.assertTrue(
        line.contains(Items.DIAMOND.getName().getString()),
        "The second largest should be named too, got \"" + line + "\"");
    context.assertTrue(
        line.contains("1 other kind") && !line.contains("1 other kinds"),
        "One unnamed kind should read in the singular, got \"" + line + "\"");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aLineWithSeveralUnnamedKindsCountsThemInThePlural(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(0, new ItemStack(Items.COBBLESTONE, 60));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 40));
    player.getInventory().setStack(1, new ItemStack(Items.DIAMOND, 30));
    player.getInventory().setStack(2, new ItemStack(Items.OAK_LOG, 20));
    player.getInventory().setStack(3, new ItemStack(Items.BREAD, 10));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    String line = OperationMessages.forDeposit(outcome).map(Text::getString).orElse("");

    context.assertTrue(
        outcome.leftoverKinds() == 4,
        "Four kinds should stay behind, reported " + outcome.leftoverKinds());
    context.assertTrue(
        line.contains("2 other kinds"),
        "Two unnamed kinds should read in the plural, got \"" + line + "\"");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullNetworkIsAnsweredUnderItsOwnKey(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 12));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    Text line = OperationMessages.forDeposit(outcome).orElseThrow();

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "A full network should report no space, got " + outcome.result());
    context.assertTrue(
        ((TranslatableTextContent) line.getContent())
            .getKey()
            .equals("operation.total-storage.deposit.full"),
        "A full network borrowed another line, got "
            + ((TranslatableTextContent) line.getContent()).getKey());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void whatWouldNotFitIsReportedByItemRatherThanOnlyCounted(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 30));
    player.getInventory().setStack(1, new ItemStack(Items.OAK_LOG, 10));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "A full network should report no space, got " + outcome.result());
    context.assertTrue(
        outcome.leftoverKinds() == 2,
        "Two kinds stayed behind, reported " + outcome.leftoverKinds());
    context.assertTrue(
        outcome.leftovers().get(0).variant().isOf(Items.COBBLESTONE),
        "The largest leftover should be named first, got " + outcome.leftovers().get(0).variant());
    context.assertTrue(
        outcome.leftovers().get(0).amount() == 30L,
        "Counted " + outcome.leftovers().get(0).amount() + " of 30 cobblestone left behind");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theLeftoverReportMatchesWhatIsStillInTheInventory(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 12));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    long reported =
        outcome.leftovers().stream()
            .filter(left -> left.variant().isOf(Items.COBBLESTONE))
            .mapToLong(left -> left.amount())
            .sum();
    context.assertTrue(
        reported == NetworkOperationFixture.countCarried(player, Items.COBBLESTONE),
        "Reported "
            + reported
            + " left over but the player holds "
            + NetworkOperationFixture.countCarried(player, Items.COBBLESTONE));
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void freeSpaceThatOnlyFilteredContainersHoldIsNamedAsTheCause(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.WEST_BARREL,
        ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:stone")));
    player.getInventory().setStack(0, new ItemStack(Items.DIRT_PATH, STACK));
    player.getInventory().setStack(1, new ItemStack(Items.DIRT_PATH, STACK));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    Text line = OperationMessages.forDeposit(outcome).orElseThrow();

    context.assertTrue(
        outcome.result() == DepositResult.ONLY_FILTERED_SPACE,
        "Free filtered space should be named as the cause, got " + outcome.result());
    context.assertTrue(
        keyOf(line).equals("operation.total-storage.deposit.filtered_only"),
        "Free filtered space borrowed another line, got " + keyOf(line));
    context.assertTrue(
        line.getString().contains("128 Dirt Path"),
        "The line should name what was offered, read " + line.getString());
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.DIRT_PATH) == 2L * STACK,
        "The inventory should be exactly as it was");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullNetworkNamesWhatWouldNotGoIn(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIRT_PATH, 7));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    Text line = OperationMessages.forDeposit(outcome).orElseThrow();

    context.assertTrue(
        line.getString().contains("7 Dirt Path"),
        "A full network should name what would not go in, read " + line.getString());
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.DIRT_PATH) == 7L,
        "The inventory should be exactly as it was");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void manyKindsRefusedAreSummarisedRatherThanListed(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIRT_PATH, 40));
    player.getInventory().setStack(1, new ItemStack(Items.COBBLESTONE, 30));
    player.getInventory().setStack(2, new ItemStack(Items.OAK_LOG, 20));
    player.getInventory().setStack(3, new ItemStack(Items.APPLE, 10));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    String line = OperationMessages.forDeposit(outcome).orElseThrow().getString();

    context.assertTrue(
        line.contains("40 Dirt Path") && line.contains("30 Cobblestone"),
        "The two largest kinds should be named, read " + line);
    context.assertTrue(
        line.contains("and 2 other kinds"), "The rest should be summarised, read " + line);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void freeGeneralSlotsThatRefuseTheItemAreNotBlamedOnFilters(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    context.setBlockState(
        NetworkOperationFixture.WEST_BARREL, Blocks.SHULKER_BOX.getDefaultState());
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.SHULKER_BOX, 1));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    Text line = OperationMessages.forDeposit(outcome).orElseThrow();

    context.assertTrue(
        outcome.result() == DepositResult.NO_FITTING_SPACE,
        "A shulker box refused by an empty shulker box should say the free slots will not hold"
            + " it, got "
            + outcome.result());
    context.assertTrue(
        keyOf(line).equals("operation.total-storage.deposit.unfitting"),
        "Free general slots borrowed another line, got " + keyOf(line));
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.SHULKER_BOX) == 1L,
        "The inventory should be exactly as it was");
    NetworkOperationFixture.finish(context, player);
  }

  private static String keyOf(Text line) {
    return ((TranslatableTextContent) line.getContent()).getKey();
  }
}
