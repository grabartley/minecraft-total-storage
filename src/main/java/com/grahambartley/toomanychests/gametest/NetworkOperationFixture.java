package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.operation.NetworkJob;
import com.grahambartley.toomanychests.operation.NetworkOperationContext;
import com.grahambartley.toomanychests.operation.OperationReport;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

final class NetworkOperationFixture {
  static final String CONTAINER_PAD = "too-many-chests:container_pad";

  static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  static final BlockPos POWER = new BlockPos(3, 1, 2);
  static final BlockPos EAST_BARREL = new BlockPos(4, 1, 3);

  static final BlockPos FAR_BARREL = new BlockPos(5, 1, 3);

  static final BlockPos WEST_BARREL = new BlockPos(2, 1, 3);
  static final BlockPos STANDING = new BlockPos(3, 1, 4);

  static final BlockPos NORTH_TERMINAL = new BlockPos(3, 1, 1);

  static final BlockPos SHARED_BARREL = new BlockPos(4, 1, 2);

  private static final int RADIUS = 1;

  private NetworkOperationFixture() {}

  static final int BUDGET = 64;

  private static final int MOST_STEPS_A_TEST_NEEDS = 64;

  static NetworkOperationContext operationContext(TestContext context, ServerPlayerEntity player) {
    return new NetworkOperationContext(
        context.getWorld(), context.getAbsolutePos(TERMINAL), player.getUuid());
  }

  static OperationReport runToCompletion(TestContext context, NetworkJob job) {
    int steps = 0;
    while (job.advance(BUDGET).isRunning()) {
      steps++;
      context.assertTrue(
          steps < MOST_STEPS_A_TEST_NEEDS, "The operation never finished, it is still running");
    }
    return job.report();
  }

  static BarrelBlockEntity placeBarrel(TestContext context, BlockPos where) {
    context.setBlockState(where, Blocks.BARREL.getDefaultState());
    return context.getBlockEntity(where);
  }

  static BarrelBlockEntity stockedBarrel(
      TestContext context, BlockPos where, Item item, int count) {
    final BarrelBlockEntity barrel = placeBarrel(context, where);
    barrel.setStack(0, new ItemStack(item, count));
    barrel.markDirty();
    return barrel;
  }

  static BarrelBlockEntity fullBarrel(TestContext context, BlockPos where, Item item) {
    final BarrelBlockEntity barrel = placeBarrel(context, where);
    for (int slot = 0; slot < barrel.size(); slot++) {
      barrel.setStack(slot, new ItemStack(item, item.getMaxCount()));
    }
    barrel.markDirty();
    return barrel;
  }

  static ServerPlayerEntity openTerminal(TestContext context) {
    final ServerPlayerEntity player = standingPlayer(context);
    placeTerminal(context, player);
    context.useBlock(TERMINAL, player);
    return player;
  }

  static ServerPlayerEntity alsoOpen(TestContext context) {
    final ServerPlayerEntity player = standingPlayer(context);
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(settings -> settings.withAccessMode(TerminalAccessMode.OPEN));
    context.useBlock(TERMINAL, player);
    return player;
  }

  static ServerPlayerEntity openNorthTerminal(TestContext context) {
    return openNorthTerminalFor(context, standingPlayer(context));
  }

  static ServerPlayerEntity openNorthTerminalFor(TestContext context, ServerPlayerEntity player) {
    context.setBlockState(NORTH_TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        context.getAbsolutePos(NORTH_TERMINAL),
        context.getBlockState(NORTH_TERMINAL),
        player,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context
        .<TerminalBlockEntity>getBlockEntity(NORTH_TERMINAL)
        .updateSettings(settings -> settings.withRadius(RADIUS));
    TerminalNetworks.power()
        .signalChanged(
            context.getWorld(),
            context.getAbsolutePos(NORTH_TERMINAL),
            context.getBlockState(NORTH_TERMINAL));
    context.useBlock(NORTH_TERMINAL, player);
    return player;
  }

  static void assign(TestContext context, BlockPos container, ContainerRole role) {
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(
            settings -> settings.withContainerRole(context.getAbsolutePos(container), role));
  }

  static void assign(TestContext context, BlockPos container, ContainerAssignment assignment) {
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(
            settings ->
                settings.withContainers(
                    settings.containers().with(context.getAbsolutePos(container), assignment)));
  }

  static void fileUnderCustomTag(TestContext context, String tag, String itemId) {
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(
            settings ->
                settings.withTags(settings.tags().withCustomTag(tag).withItem(tag, itemId)));
  }

  static void widenWithoutRescanning(TestContext context, int radius) {
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(settings -> settings.withRadius(radius));
  }

  static ServerPlayerEntity watching(ServerPlayerEntity player) {
    ModNetworking.listings()
        .request(
            player,
            new ListingRequestPayload(
                handlerOf(player).syncId, 0, 10, "", TagCombinationMode.DEFAULT, Set.of()));
    return player;
  }

  static TerminalScreenHandler handlerOf(ServerPlayerEntity player) {
    return (TerminalScreenHandler) player.currentScreenHandler;
  }

  static long countIn(BarrelBlockEntity barrel, Item item) {
    long total = 0L;
    for (int slot = 0; slot < barrel.size(); slot++) {
      final ItemStack stack = barrel.getStack(slot);
      if (stack.isOf(item)) {
        total += stack.getCount();
      }
    }
    return total;
  }

  static long countCarried(ServerPlayerEntity player, Item item) {
    long total = 0L;
    for (int slot = 0; slot < player.getInventory().size(); slot++) {
      final ItemStack stack = player.getInventory().getStack(slot);
      if (stack.isOf(item)) {
        total += stack.getCount();
      }
    }
    final ItemStack cursor = player.currentScreenHandler.getCursorStack();
    return cursor.isOf(item) ? total + cursor.getCount() : total;
  }

  static long countInWorld(TestContext context, Item item, ServerPlayerEntity... players) {
    final long[] total = {0L};
    context.forEachRelativePos(where -> total[0] += countInContainerAt(context, where, item));
    for (final ItemEntity dropped :
        context
            .getWorld()
            .getEntitiesByClass(ItemEntity.class, context.getTestBox(), any -> true)) {
      if (dropped.getStack().isOf(item)) {
        total[0] += dropped.getStack().getCount();
      }
    }
    for (final ServerPlayerEntity player : players) {
      total[0] += countCarried(player, item);
    }
    return total[0];
  }

  static void assertConserved(
      TestContext context, long before, Item item, ServerPlayerEntity... players) {
    final long after = countInWorld(context, item, players);
    context.assertTrue(
        after == before,
        "The world held " + before + " " + item.getName().getString() + " and now holds " + after);
  }

  private static long countInContainerAt(TestContext context, BlockPos where, Item item) {
    if (!(context.getWorld().getBlockEntity(context.getAbsolutePos(where))
        instanceof Inventory inventory)) {
      return 0L;
    }
    long total = 0L;
    for (int slot = 0; slot < inventory.size(); slot++) {
      final ItemStack stack = inventory.getStack(slot);
      if (stack.isOf(item)) {
        total += stack.getCount();
      }
    }
    return total;
  }

  static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
      player.closeHandledScreen();
      TerminalAccessControl.viewers().closedEverything(player.getUuid());
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(NORTH_TERMINAL));
    context.complete();
  }

  static ServerPlayerEntity standingPlayer(TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final BlockPos standing = context.getAbsolutePos(STANDING);
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);
    player.getInventory().clear();
    return player;
  }

  private static void placeTerminal(TestContext context, ServerPlayerEntity owner) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        context.getAbsolutePos(TERMINAL),
        context.getBlockState(TERMINAL),
        owner,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context.<TerminalBlockEntity>getBlockEntity(TERMINAL).updateSettings(s -> s.withRadius(RADIUS));
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
  }
}
