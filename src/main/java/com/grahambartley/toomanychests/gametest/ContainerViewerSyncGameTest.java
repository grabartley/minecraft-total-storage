package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.sort.ContainerSortService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Reaching everybody who has a container open by hand, which only exists on a running server.
 *
 * <p>Two players at one chest hold two separate screen handlers over one inventory, and a terminal
 * moving items through the network is a third writer neither of them can see. What is proven here
 * is that everybody looking at the container is told, and that a player at a different one is left
 * alone.
 *
 * <p>A double chest is the case worth its own tests. A change names one block entity while the
 * player who opened the chest by hand is looking at one inventory spanning both halves, and every
 * player who opens it gets their own spanning inventory, so neither end of the match can be taken
 * at face value.
 */
public final class ContainerViewerSyncGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "container-viewers";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos SHARED_CHEST = new BlockPos(3, 1, 3);
  private static final BlockPos OTHER_CHEST = new BlockPos(1, 1, 1);

  /** A chest facing north pairs its left half with the block to its east. */
  private static final BlockPos DOUBLE_LEFT = new BlockPos(1, 1, 5);

  private static final BlockPos DOUBLE_RIGHT = new BlockPos(2, 1, 5);
  private static final BlockPos OTHER_DOUBLE_LEFT = new BlockPos(4, 1, 5);
  private static final BlockPos OTHER_DOUBLE_RIGHT = new BlockPos(5, 1, 5);

  private final ContainerSortService sorting = new ContainerSortService();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bothPlayersAtOneChestAreResynced(TestContext context) {
    context.setBlockState(SHARED_CHEST, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity sorter = openedOn(context, SHARED_CHEST);
    ServerPlayerEntity watcher = openedOn(context, SHARED_CHEST);
    Inventory chest = context.getBlockEntity(SHARED_CHEST);

    int resynced = ContainerViewerSync.resync(context.getWorld().getServer(), chest);

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

    int resynced = ContainerViewerSync.resync(context.getWorld().getServer(), chest);

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
        ContainerViewerSync.resync(context.getWorld().getServer(), null),
        0,
        "No inventory, no resync");
    context.assertEquals(
        ContainerViewerSync.resync(null, context.getBlockEntity(SHARED_CHEST)),
        0,
        "No server, no resync");
    context.complete();
  }

  /** One half changing is the whole screen going stale, because the screen spans both. */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aViewerOfADoubleChestIsResyncedByEitherHalfOfIt(TestContext context) {
    placeDoubleChest(context, DOUBLE_LEFT, DOUBLE_RIGHT);
    ServerPlayerEntity watcher = openedOn(context, DOUBLE_LEFT);

    int byLeft =
        ContainerViewerSync.resync(
            context.getWorld().getServer(), context.<ChestBlockEntity>getBlockEntity(DOUBLE_LEFT));
    int byRight =
        ContainerViewerSync.resync(
            context.getWorld().getServer(), context.<ChestBlockEntity>getBlockEntity(DOUBLE_RIGHT));

    context.assertEquals(byLeft, 1, "The left half should have resynced the screen over both");
    context.assertEquals(byRight, 1, "The right half should have resynced the screen over both");
    finish(context, watcher);
  }

  /** Halves of one double chest are not halves of another. */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aViewerOfADifferentDoubleChestIsLeftAlone(TestContext context) {
    placeDoubleChest(context, DOUBLE_LEFT, DOUBLE_RIGHT);
    placeDoubleChest(context, OTHER_DOUBLE_LEFT, OTHER_DOUBLE_RIGHT);
    ServerPlayerEntity watcher = openedOn(context, OTHER_DOUBLE_LEFT);

    int resynced =
        ContainerViewerSync.resync(
            context.getWorld().getServer(), context.<ChestBlockEntity>getBlockEntity(DOUBLE_LEFT));

    context.assertEquals(resynced, 0, "A viewer of another double chest should hear nothing");
    finish(context, watcher);
  }

  /**
   * Sorting a double chest, which is the case a screen instance cannot answer. Every player who
   * opens one gets their own inventory spanning the two halves, so the sorter's own instance is not
   * the one the bystander is holding and only the block entities are common to both.
   */
  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void bothPlayersAtOneDoubleChestAreResyncedByASort(TestContext context) {
    placeDoubleChest(context, DOUBLE_LEFT, DOUBLE_RIGHT);
    context
        .<ChestBlockEntity>getBlockEntity(DOUBLE_RIGHT)
        .setStack(2, new ItemStack(Items.STONE, 8));
    ServerPlayerEntity sorter = openedOn(context, DOUBLE_LEFT);
    ServerPlayerEntity watcher = openedOn(context, DOUBLE_LEFT);
    long before = revisionOf(watcher);

    sorting.sort(sorter, sorter.currentScreenHandler.syncId);

    context.assertTrue(
        revisionOf(watcher) > before,
        "The other player at the double chest was left looking at the old layout");
    finish(context, sorter, watcher);
  }

  private static int revisionOf(ServerPlayerEntity player) {
    return player.currentScreenHandler.getRevision();
  }

  private static void placeDoubleChest(TestContext context, BlockPos left, BlockPos right) {
    context.setBlockState(
        left,
        Blocks.CHEST
            .getDefaultState()
            .with(ChestBlock.FACING, Direction.NORTH)
            .with(ChestBlock.CHEST_TYPE, ChestType.LEFT));
    context.setBlockState(
        right,
        Blocks.CHEST
            .getDefaultState()
            .with(ChestBlock.FACING, Direction.NORTH)
            .with(ChestBlock.CHEST_TYPE, ChestType.RIGHT));
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
