package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Whether a listing keeps itself current while nobody touches it.
 *
 * <p>Driven through the broadcast the mod really registers rather than by calling it, so what is
 * proven is that the tick hook exists and fires. A page reaching a client cannot be seen from here:
 * a gametest's mock player never declares the mod's channels, so the send goes nowhere. Every page
 * built is stamped with a new revision though, and the stamp advancing is the same evidence a
 * packet would have been.
 */
public final class TerminalListingBroadcastGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-listing-broadcast";
  private static final int TICK_LIMIT = 60;
  private static final int SETTLE_TICKS = 4;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);
  private static final BlockPos STANDING = new BlockPos(3, 1, 4);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aContainerChangingSendsTheViewerAFreshPage(TestContext context) {
    ServerPlayerEntity player = watchingTerminal(context);
    TerminalScreenHandler handler = handlerOf(player);
    long before = handler.listing().revision();

    fillBarrelFurther(context);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              handler.listing().revision() > before,
              "A container changing should have pushed a page, stamp stayed at " + before);
          finish(context, player);
        });
  }

  /** A network nobody has changed costs one boolean per tick, and reads nothing. */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNetworkNobodyHasChangedIsNeverReadAgain(TestContext context) {
    ServerPlayerEntity player = watchingTerminal(context);
    TerminalScreenHandler handler = handlerOf(player);
    long before = handler.listing().revision();

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              handler.listing().revision() == before,
              "An unchanged network should push nothing, stamp moved to "
                  + handler.listing().revision());
          finish(context, player);
        });
  }

  /**
   * A network with no viewer has nothing to tell anyone, so its index is left owing a recompute
   * rather than being read on a tick for a screen that is not open.
   */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTerminalNobodyIsWatchingIsLeftUnread(TestContext context) {
    ServerPlayerEntity player = watchingTerminal(context);
    stopWatching(context, player);

    fillBarrelFurther(context);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          NetworkMembership network =
              TerminalNetworks.power()
                  .network(context.getWorld(), context.getAbsolutePos(TERMINAL))
                  .orElse(null);
          context.assertTrue(network != null, "The terminal should still be running its network");
          context.assertTrue(
              network.session().isStale(),
              "A network nobody is watching should still owe the recompute nobody asked for");
          TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
          context.complete();
        });
  }

  /** A screen that has asked for nothing has no window to answer, so nothing is pushed to it. */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aScreenThatHasAskedForNothingIsNotPushedTo(TestContext context) {
    ServerPlayerEntity player = openTerminal(context);
    TerminalScreenHandler handler = handlerOf(player);

    fillBarrelFurther(context);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertFalse(
              handler.listing().hasWindow(), "The screen should still be showing no window");
          context.assertTrue(
              handler.listing().revision() == 0L,
              "Nothing should have been stamped for a screen that asked for nothing");
          finish(context, player);
        });
  }

  private static ServerPlayerEntity watchingTerminal(TestContext context) {
    ServerPlayerEntity player = openTerminal(context);
    TerminalScreenHandler handler = handlerOf(player);
    ModNetworking.listings()
        .request(
            player,
            new ListingRequestPayload(
                handler.syncId, 0, 10, "", TagCombinationMode.DEFAULT, Set.of()));
    return player;
  }

  private static void fillBarrelFurther(TestContext context) {
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    barrel.setStack(4, new ItemStack(Items.DIAMOND, 32));
    barrel.markDirty();
  }

  private static ServerPlayerEntity openTerminal(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(STANDING);
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);

    context.setBlockState(BARREL, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    barrel.setStack(0, new ItemStack(Items.DIAMOND, 64));
    barrel.markDirty();

    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        context.getAbsolutePos(TERMINAL),
        context.getBlockState(TERMINAL),
        player,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context.<TerminalBlockEntity>getBlockEntity(TERMINAL).updateSettings(s -> s.withRadius(1));
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
    context.useBlock(TERMINAL, player);
    return player;
  }

  private static TerminalScreenHandler handlerOf(ServerPlayerEntity player) {
    return (TerminalScreenHandler) player.currentScreenHandler;
  }

  private static void stopWatching(TestContext context, ServerPlayerEntity player) {
    player.closeHandledScreen();
    TerminalAccessControl.viewers().closedEverything(player.getUuid());
    context.getWorld().getServer().getPlayerManager().remove(player);
  }

  private static void finish(TestContext context, ServerPlayerEntity player) {
    stopWatching(context, player);
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
    context.complete();
  }
}
