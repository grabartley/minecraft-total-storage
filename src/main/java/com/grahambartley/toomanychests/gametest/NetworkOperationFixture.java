package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * One terminal, its containers, and a player standing in front of it, built the same way for every
 * test of an operation.
 *
 * <p>Order matters and is the reason this is shared rather than copied: containers have to exist
 * before the terminal is powered, because per ADR 0007 powering on is the one discovery scan a
 * network gets. A test that placed a barrel afterwards would be testing an empty network and
 * passing for the wrong reason.
 */
final class NetworkOperationFixture {
  static final String CONTAINER_PAD = "too-many-chests:container_pad";

  static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  static final BlockPos POWER = new BlockPos(3, 1, 2);
  static final BlockPos EAST_BARREL = new BlockPos(4, 1, 3);
  static final BlockPos WEST_BARREL = new BlockPos(2, 1, 3);
  static final BlockPos STANDING = new BlockPos(3, 1, 4);

  /** Big enough to reach the barrels either side of the terminal and nothing beyond the pad. */
  private static final int RADIUS = 1;

  private NetworkOperationFixture() {}

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

  /** Fills every slot of a barrel so the network has nowhere left to put anything. */
  static BarrelBlockEntity fullBarrel(TestContext context, BlockPos where, Item item) {
    final BarrelBlockEntity barrel = placeBarrel(context, where);
    for (int slot = 0; slot < barrel.size(); slot++) {
      barrel.setStack(slot, new ItemStack(item, item.getMaxCount()));
    }
    barrel.markDirty();
    return barrel;
  }

  /** Powers the terminal on and opens it, which is the only order a network can be built in. */
  static ServerPlayerEntity openTerminal(TestContext context) {
    final ServerPlayerEntity player = standingPlayer(context);
    placeTerminal(context, player);
    context.useBlock(TERMINAL, player);
    return player;
  }

  /**
   * A second player at the same terminal, for the races only two screens can produce. The terminal
   * is opened up first, because a terminal is owner-locked by default per ADR 0008 and the second
   * player is not its owner.
   */
  static ServerPlayerEntity alsoOpen(TestContext context) {
    final ServerPlayerEntity player = standingPlayer(context);
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(settings -> settings.withAccessMode(TerminalAccessMode.OPEN));
    context.useBlock(TERMINAL, player);
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

  /** Files an item under a tag this network invented, which lives on the terminal itself. */
  static void fileUnderCustomTag(TestContext context, String tag, String itemId) {
    context
        .<TerminalBlockEntity>getBlockEntity(TERMINAL)
        .updateSettings(
            settings ->
                settings.withTags(settings.tags().withCustomTag(tag).withItem(tag, itemId)));
  }

  static TerminalScreenHandler handlerOf(ServerPlayerEntity player) {
    return (TerminalScreenHandler) player.currentScreenHandler;
  }

  /** Everything one barrel is holding, which is what a conservation check counts. */
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

  static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
      player.closeHandledScreen();
      TerminalAccessControl.viewers().closedEverything(player.getUuid());
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
    context.complete();
  }

  private static ServerPlayerEntity standingPlayer(TestContext context) {
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
