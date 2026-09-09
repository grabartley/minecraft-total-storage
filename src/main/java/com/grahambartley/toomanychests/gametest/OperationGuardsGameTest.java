package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigBuilder;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.operation.DepositOutcome;
import com.grahambartley.toomanychests.operation.DepositResult;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.PlayerSlots;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class OperationGuardsGameTest implements FabricGameTest {
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private static final String SET_COOLDOWN_BATCH = "guards-cooldown-set";

  private static final String NO_COOLDOWN_BATCH = "guards-cooldown-off";
  private static final String HOTBAR_BATCH = "guards-hotbar";

  private final DepositService deposits = new DepositService();

  @BeforeBatch(batchId = SET_COOLDOWN_BATCH)
  public void clearBeforeSetCooldown(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @BeforeBatch(batchId = NO_COOLDOWN_BATCH)
  public void clearBeforeNoCooldown(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @BeforeBatch(batchId = HOTBAR_BATCH)
  public void clearBeforeHotbar(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = SET_COOLDOWN_BATCH,
      tickLimit = TICK_LIMIT)
  public void aSecondDepositAllInTheSameTickIsRefusedAndMovesNothing(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;
    TooManyChestsConfig original = ServerConfigService.current();

    DepositOutcome second;
    try {
      withCooldown(context, 10);
      deposits.depositAll(player, syncId);
      player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, STACK));
      second = deposits.depositAll(player, syncId);
    } finally {
      restore(context, original);
    }

    context.assertTrue(
        second.result() == DepositResult.RATE_LIMITED,
        "A second deposit in the same tick should be rate limited, got " + second.result());
    context.assertTrue(
        player.getInventory().getStack(0).getCount() == STACK,
        "A rate limited deposit must move nothing");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE) == 0L,
        "A rate limited deposit reached the containers anyway");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = NO_COOLDOWN_BATCH,
      tickLimit = TICK_LIMIT)
  public void aServerThatSetTheCooldownToNothingIsNeverRateLimited(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;
    TooManyChestsConfig original = ServerConfigService.current();

    DepositOutcome second;
    try {
      withCooldown(context, 0);
      deposits.depositAll(player, syncId);
      player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, STACK));
      second = deposits.depositAll(player, syncId);
    } finally {
      restore(context, original);
    }

    context.assertTrue(
        second.result() != DepositResult.RATE_LIMITED,
        "A cooldown of nothing should never refuse, got " + second.result());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = HOTBAR_BATCH,
      tickLimit = TICK_LIMIT)
  public void depositAllReadsTheServersHotbarSetting(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 8));
    player.getInventory().setStack(PlayerSlots.HOTBAR_SLOTS, new ItemStack(Items.OAK_LOG, 8));
    TooManyChestsConfig original = ServerConfigService.current();

    try {
      withHotbarSkipped(context, true);
      deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    } finally {
      restore(context, original);
    }

    context.assertTrue(
        player.getInventory().getStack(0).getCount() == 8,
        "The hotbar was emptied while the server asked for it to be kept");
    context.assertTrue(
        player.getInventory().getStack(PlayerSlots.HOTBAR_SLOTS).isEmpty(),
        "The main inventory should still have been emptied");
    NetworkOperationFixture.finish(context, player);
  }

  private static void withHotbarSkipped(TestContext context, boolean skip) {
    ServerConfigService.update(
        context.getWorld().getServer(),
        ConfigBuilder.from(ServerConfigService.current()).depositAllSkipsHotbar(skip).build());
  }

  private static void withCooldown(TestContext context, int ticks) {
    ServerConfigService.update(
        context.getWorld().getServer(),
        ConfigBuilder.from(ServerConfigService.current())
            .expensiveOperationCooldownTicks(ticks)
            .build());
  }

  private static void restore(TestContext context, TooManyChestsConfig original) {
    ServerConfigService.update(context.getWorld().getServer(), original);
  }
}
