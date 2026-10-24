package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.network.DepositRequestPayload;
import com.grahambartley.totalstorage.network.TagPanelRequestPayload;
import com.grahambartley.totalstorage.network.TerminalSettingsNetworking;
import com.grahambartley.totalstorage.network.WithdrawRequestPayload;
import com.grahambartley.totalstorage.operation.DepositService;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.operation.WithdrawDestination;
import com.grahambartley.totalstorage.operation.WithdrawService;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalTagPanelService;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class TerminalTagPanelServiceGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-tag-panel";
  private static final int TICK_LIMIT = 60;
  private static final int SETTLE_TICKS = 4;
  private static final String TAG = "castle";
  private static final String DIAMOND_ID = "minecraft:diamond";

  private final TerminalTagPanelService panels = TerminalSettingsNetworking.tagPanels();
  private final DepositService deposits = new DepositService();
  private final WithdrawService withdrawals = new WithdrawService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aDepositSendsTheOpenPanelItsNewCounts(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = panelOpenOnEmptyNetwork(context);
    context.assertTrue(matching(player) == 0, "An empty network should match nothing");
    player.getInventory().setStack(3, new ItemStack(Items.DIAMOND, 64));

    deposits.deposit(player, DepositRequestPayload.wholeSlot(syncId(player), 3));

    context.assertTrue(
        matching(player) > 0,
        "The deposit should have sent the panel a count for the diamonds, it still reads "
            + matching(player));
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void withdrawingTheLastOfAnItemDropsItsTagCount(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 5);
    ServerPlayerEntity player = panelOpenOnEmptyNetwork(context);
    context.assertTrue(matching(player) > 0, "The stocked diamonds should be counted");

    withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            syncId(player), ItemVariant.of(Items.DIAMOND), 5, WithdrawDestination.CURSOR));

    context.assertTrue(
        matching(player) == 0,
        "Withdrawing the last diamond should have sent a zero count, it reads " + matching(player));
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerFilledByAnythingElseReachesThePanelThroughTheTickSweep(
      TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = panelOpenOnEmptyNetwork(context);

    barrel.setStack(0, new ItemStack(Items.DIAMOND, 16));
    barrel.markDirty();

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              matching(player) > 0,
              "The tick sweep should have sent the panel the new count, it reads "
                  + matching(player));
          NetworkOperationFixture.finish(context, player);
        });
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnchangedNetworkSendsThePanelNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 5);
    ServerPlayerEntity player = panelOpenOnEmptyNetwork(context);
    NetworkTagView before = handler(player).tags().lastSent().orElseThrow();

    boolean sent = panels.contentsChanged(player);

    context.assertFalse(sent, "Nothing changed, so nothing should have been sent");
    context.assertTrue(
        handler(player).tags().lastSent().orElseThrow() == before,
        "The panel's last view should be the one it already had");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aClosedPanelIsNeverSentCounts(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    NetworkOperationFixture.fileUnderCustomTag(context, TAG, DIAMOND_ID);
    player.getInventory().setStack(3, new ItemStack(Items.DIAMOND, 64));

    deposits.deposit(player, DepositRequestPayload.wholeSlot(syncId(player), 3));

    context.assertTrue(
        handler(player).tags().lastSent().isEmpty(),
        "A viewer without the panel open should have been sent nothing");
    NetworkOperationFixture.finish(context, player);
  }

  private ServerPlayerEntity panelOpenOnEmptyNetwork(TestContext context) {
    ServerPlayerEntity player =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    NetworkOperationFixture.fileUnderCustomTag(context, TAG, DIAMOND_ID);
    panels.request(player, TagPanelRequestPayload.opened(syncId(player)));
    return player;
  }

  private static int matching(ServerPlayerEntity player) {
    return handler(player).tags().lastSent().orElseThrow().tags().stream()
        .filter(tag -> tag.id().equals(TAG))
        .mapToInt(NetworkTagEntry::matchingItems)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("the panel never listed " + TAG));
  }

  private static TerminalScreenHandler handler(ServerPlayerEntity player) {
    return NetworkOperationFixture.handlerOf(player);
  }

  private static int syncId(ServerPlayerEntity player) {
    return handler(player).syncId;
  }
}
