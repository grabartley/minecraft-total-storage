package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.sort.ContainerSortService;
import com.grahambartley.toomanychests.sort.SortViewerSync;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Two players at one chest, which is the case that only exists on a running server.
 *
 * <p>They hold two separate screen handlers over one inventory, so the sorting player's own resync
 * reaches nobody else. What is proven here is that the other one is told as well, and that a player
 * at a different chest is left alone.
 */
public final class SortViewerSyncGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "sort-viewers";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos SHARED_CHEST = new BlockPos(3, 1, 3);
  private static final BlockPos OTHER_CHEST = new BlockPos(1, 1, 1);

  private final ContainerSortService sorting = new ContainerSortService();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bothPlayersAtOneChestAreResynced(TestContext context) {
    context.setBlockState(SHARED_CHEST, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity sorter = openedOn(context, SHARED_CHEST);
    ServerPlayerEntity watcher = openedOn(context, SHARED_CHEST);
    Inventory chest = context.getBlockEntity(SHARED_CHEST);

    int resynced = SortViewerSync.resync(context.getWorld().getServer(), chest);

    context.assertEquals(resynced, 2, "Both viewers of one chest should be resynced");
    finish(context, sorter, watcher);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPlayerAtADifferentChestIsLeftAlone(TestContext context) {
    context.setBlockState(SHARED_CHEST, Blocks.CHEST.getDefaultState());
    context.setBlockState(OTHER_CHEST, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity sorter = openedOn(context, SHARED_CHEST);
    ServerPlayerEntity elsewhere = openedOn(context, OTHER_CHEST);
    Inventory chest = context.getBlockEntity(SHARED_CHEST);

    int resynced = SortViewerSync.resync(context.getWorld().getServer(), chest);

    context.assertEquals(resynced, 1, "Only the viewers of the sorted chest should be resynced");
    finish(context, sorter, elsewhere);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aSortByOnePlayerIsVisibleThroughTheOthersScreenHandler(TestContext context) {
    context.setBlockState(SHARED_CHEST, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity sorter = openedOn(context, SHARED_CHEST);
    ServerPlayerEntity watcher = openedOn(context, SHARED_CHEST);
    Inventory chest = context.getBlockEntity(SHARED_CHEST);
    chest.setStack(4, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(11, new ItemStack(Items.COBBLESTONE, 30));

    sorting.sort(sorter, sorter.currentScreenHandler.syncId);

    ItemStack throughWatcher = watcher.currentScreenHandler.getSlot(0).getStack();
    context.assertTrue(
        throughWatcher.isOf(Items.COBBLESTONE) && throughWatcher.getCount() == 50,
        "The watcher's screen should show the merged stack, found " + throughWatcher);
    finish(context, sorter, watcher);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void resyncingWithoutAnInventoryOrAServerDoesNothing(TestContext context) {
    context.setBlockState(SHARED_CHEST, Blocks.CHEST.getDefaultState());
    context.assertEquals(
        SortViewerSync.resync(context.getWorld().getServer(), null), 0, "No inventory, no resync");
    context.assertEquals(
        SortViewerSync.resync(null, context.getBlockEntity(SHARED_CHEST)),
        0,
        "No server, no resync");
    context.complete();
  }

  private static ServerPlayerEntity openedOn(TestContext context, BlockPos relative) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos absolute = context.getAbsolutePos(relative);
    NamedScreenHandlerFactory factory =
        context.getBlockState(relative).createScreenHandlerFactory(context.getWorld(), absolute);
    player.openHandledScreen(factory);
    return player;
  }

  private static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.closeHandledScreen();
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    context.complete();
  }
}
