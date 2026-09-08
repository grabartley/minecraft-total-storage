package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.operation.DepositOutcome;
import com.grahambartley.toomanychests.operation.DepositResult;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.OperationMessages;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

/**
 * What a deposit says it could not store, against a real inventory.
 *
 * <p>Separate from {@code DepositServiceGameTest} because it is a different question: that one asks
 * where items went, this one asks what the player is told about the ones that stayed. Both need a
 * world, and for the same reason: naming an item needs a registry, so an outcome that accounts for
 * its shortfall cannot be built in a plain JVM at all.
 *
 * <p>The keys are asserted rather than the English. The English is a fallback for a client whose
 * resource pack has not caught up, and a branch that quietly reused another branch's key would say
 * the wrong thing in every language but this one.
 */
public final class DepositLeftoverReportGameTest implements FabricGameTest {
  private static final String BATCH = "deposit-leftover-report";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private final DepositService deposits = new DepositService();

  /**
   * The partial path accounts for its shortfall by item, not only by number.
   *
   * <p>Separate from the no-space case because the two are gathered by different code: one lists
   * every offer, the other lists what each offer could not place. The same variant is spread over
   * two slots here, so a report that failed to gather them would say two entries where a player
   * sees one kind.
   */
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

  /**
   * A deposit that fits entirely accounts for nothing, which is the other half of the rule: an
   * outcome may not invent a shortfall any more than it may hide one.
   */
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

  /** The partial line names the item, which is the whole reason the breakdown is gathered. */
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
            .equals("operation.too-many-chests.deposit.partial"),
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

  /**
   * More kinds than the line will name. The two largest are named and the rest are counted, so both
   * the separator between them and the "and N other kinds" tail are exercised: a partial
   * deposit-all routinely leaves more behind than a sentence can carry.
   */
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

  /** Two or more unnamed kinds read in the plural, which is the other half of that rule. */
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

  /** A network with no room says so under its own key rather than borrowing the partial one. */
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
            .equals("operation.too-many-chests.deposit.full"),
        "A full network borrowed another line, got "
            + ((TranslatableTextContent) line.getContent()).getKey());
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * What could not be stored is named, not merely counted. "Sixty left over" reads as a failure
   * where "sixty cobblestone left over" reads as a chest that filled up, which is actionable.
   */
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

  /**
   * The report has to match what is really still in the inventory, or it is worse than no report: a
   * player who is told the wrong thing goes looking for items that never moved.
   */
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
}
