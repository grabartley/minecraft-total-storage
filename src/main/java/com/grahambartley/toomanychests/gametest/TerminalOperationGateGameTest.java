package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class TerminalOperationGateGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-operation-gate";
  private static final int TICK_LIMIT = 60;

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerLookingAtTheirOwnTerminalReachesItsLiveNetwork(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);

    long listed =
        TerminalOperationGate.open(player, handler.syncId)
            .map(open -> open.network().index().countOf(ItemVariant.of(Items.DIAMOND)))
            .orElse(0L);

    context.assertTrue(
        listed == 64L,
        "An open terminal should resolve to the network holding its diamonds, found " + listed);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRequestNamingAnotherScreenReachesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);

    context.assertTrue(
        TerminalOperationGate.open(player, handler.syncId + 1).isEmpty(),
        "A request naming another screen must reach no network");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWithNoTerminalOpenReachesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;
    player.closeHandledScreen();

    context.assertTrue(
        TerminalOperationGate.open(player, syncId).isEmpty(),
        "A closed screen must reach no network");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTerminalThatHasLostPowerReachesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    context.setBlockState(NetworkOperationFixture.POWER, Blocks.AIR);
    TerminalNetworks.power()
        .unloaded(context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));

    context.assertTrue(
        TerminalOperationGate.open(player, handler.syncId).isEmpty(),
        "An unpowered terminal must reach no network");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoseAccessWasTakenAwayReachesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 64);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    context
        .<TerminalBlockEntity>getBlockEntity(NetworkOperationFixture.TERMINAL)
        .updateSettings(
            settings ->
                settings.withAccessMode(TerminalAccessMode.OWNER).withOwner(UUID.randomUUID()));

    context.assertTrue(
        TerminalOperationGate.open(player, handler.syncId).isEmpty(),
        "A revoked player must reach no network");
    NetworkOperationFixture.finish(context, player);
  }
}
