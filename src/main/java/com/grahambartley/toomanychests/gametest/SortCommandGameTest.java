package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.PlayerSlots;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class SortCommandGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "sort-command";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos IN_VIEW = new BlockPos(3, 1, 3);
  private static final BlockPos BESIDE = new BlockPos(4, 1, 3);
  private static final BlockPos STANDING = new BlockPos(3, 1, 5);
  private static final BlockPos FLOOR = new BlockPos(3, 0, 5);
  private static final BlockPos OUT_OF_REACH = new BlockPos(0, 1, 0);
  private static final BlockPos FAR_CORNER = new BlockPos(6, 1, 6);

  private static final int FIRST_PART_SLOT = 12;
  private static final int SECOND_PART_SLOT = 20;

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aChestInViewIsSortedByAPlayerWithNoScreenOpen(TestContext context) {
    Inventory chest = placeWithPartStacks(context, IN_VIEW, Blocks.CHEST.getDefaultState());
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);
    context.assertTrue(
        player.currentScreenHandler == player.playerScreenHandler,
        "The player must have nothing but their own inventory open");

    int result = runSort(context, player);

    context.assertTrue(result == 1, "The command should report a sort, returned " + result);
    context.assertTrue(
        chest.getStack(0).isOf(Items.COBBLESTONE) && chest.getStack(0).getCount() == 50,
        "The chest's part stacks should have merged, found " + chest.getStack(0));
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "Sorting the chest in view must not touch the player's inventory");
    context.assertTrue(
        ChestBlockEntity.getPlayersLookingInChestCount(
                context.getWorld(), context.getAbsolutePos(IN_VIEW))
            == 0,
        "The chest must be closed again once it has been sorted");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDoubleChestInViewSortsAsTheOneContainerItIs(TestContext context) {
    context.setBlockState(IN_VIEW, halfOfADoubleChest(ChestType.RIGHT));
    context.setBlockState(BESIDE, halfOfADoubleChest(ChestType.LEFT));
    Inventory near = context.getBlockEntity(IN_VIEW);
    Inventory far = context.getBlockEntity(BESIDE);
    near.setStack(10, new ItemStack(Items.COBBLESTONE, 20));
    far.setStack(20, new ItemStack(Items.COBBLESTONE, 30));
    ServerPlayerEntity player = playerAt(context, STANDING);
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    List<ItemStack> occupied = new ArrayList<>();
    occupied.addAll(nonEmpty(near));
    occupied.addAll(nonEmpty(far));
    context.assertTrue(result == 1, "The command should report a sort, returned " + result);
    context.assertTrue(
        occupied.size() == 1 && occupied.get(0).getCount() == 50,
        "Both halves should have merged into one stack of fifty, found " + occupied);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFurnaceInViewIsRefusedAndLeftExactlyAsItWas(TestContext context) {
    context.setBlockState(IN_VIEW, Blocks.FURNACE.getDefaultState());
    Inventory furnace = context.getBlockEntity(IN_VIEW);
    furnace.setStack(0, new ItemStack(Items.RAW_IRON, 5));
    furnace.setStack(1, new ItemStack(Items.COAL, 3));
    List<ItemStack> before = snapshotOf(furnace);
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "A furnace must be refused, the command returned " + result);
    context.assertTrue(
        matches(before, snapshotOf(furnace)), "The furnace must hold exactly what it held");
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "A refused furnace must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aLockedChestInViewStaysLockedAndUnsorted(TestContext context) {
    context.setBlockState(IN_VIEW, Blocks.CHEST.getDefaultState());
    LockableContainerBlockEntity chest = context.getBlockEntity(IN_VIEW);
    NbtCompound lock = new NbtCompound();
    lock.putString("Lock", "only the keyholder");
    chest.read(lock, context.getWorld().getRegistryManager());
    chest.setStack(4, new ItemStack(Items.COBBLESTONE, 20));
    chest.setStack(9, new ItemStack(Items.COBBLESTONE, 30));
    List<ItemStack> before = snapshotOf(chest);
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "A locked chest must be refused, returned " + result);
    context.assertTrue(
        matches(before, snapshotOf(chest)), "A locked chest must hold exactly what it held");
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "A locked chest must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aChestThatCannotOpenIsRefusedRatherThanSwappedForTheInventory(TestContext context) {
    Inventory chest = placeWithPartStacks(context, IN_VIEW, Blocks.CHEST.getDefaultState());
    context.setBlockState(IN_VIEW.up(), Blocks.STONE.getDefaultState());
    List<ItemStack> before = snapshotOf(chest);
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "A chest that cannot open must be refused, returned " + result);
    context.assertTrue(
        matches(before, snapshotOf(chest)), "The blocked chest must hold exactly what it held");
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "A blocked chest must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEnderChestInViewIsRefusedRatherThanSwappedForTheInventory(TestContext context) {
    context.setBlockState(IN_VIEW, Blocks.ENDER_CHEST.getDefaultState());
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "An ender chest must be refused, returned " + result);
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "An ender chest must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aSpectatorLookingAtAChestIsRefused(TestContext context) {
    Inventory chest = placeWithPartStacks(context, IN_VIEW, Blocks.CHEST.getDefaultState());
    List<ItemStack> before = snapshotOf(chest);
    FakePlayer spectator =
        FakePlayer.get(context.getWorld(), new GameProfile(UUID.randomUUID(), "spectating"));
    spectator.changeGameMode(GameMode.SPECTATOR);
    BlockPos standing = context.getAbsolutePos(STANDING);
    spectator.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);
    lookAt(context, spectator, IN_VIEW);

    int result = runSort(context, spectator);

    context.assertTrue(result == 0, "A spectator must be refused, returned " + result);
    context.assertTrue(
        matches(before, snapshotOf(chest)), "A spectator must not rearrange a chest");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aShulkerBoxWithItsLidBlockedIsRefused(TestContext context) {
    Inventory shulker = placeWithPartStacks(context, IN_VIEW, Blocks.SHULKER_BOX.getDefaultState());
    context.setBlockState(IN_VIEW.up(), Blocks.STONE.getDefaultState());
    List<ItemStack> before = snapshotOf(shulker);
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "A shut shulker box must be refused, returned " + result);
    context.assertTrue(
        matches(before, snapshotOf(shulker)), "The shut shulker box must hold what it held");
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "A shut shulker box must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBlockThatHoldsItemsButOpensNoScreenSortsTheInventory(TestContext context) {
    context.setBlockState(IN_VIEW, Blocks.CHISELED_BOOKSHELF.getDefaultState());
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 1, "The inventory should have been sorted, returned " + result);
    assertInventoryMerged(context, player);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anAnvilInViewIsRefused(TestContext context) {
    context.setBlockState(IN_VIEW, Blocks.ANVIL.getDefaultState());
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    List<ItemStack> inventoryBefore = snapshotOf(player.getInventory());
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 0, "An anvil must be refused, the command returned " + result);
    context.assertTrue(
        matches(inventoryBefore, snapshotOf(player.getInventory())),
        "A refused anvil must not fall back to sorting the inventory");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aChestOutOfReachIsLeftAloneAndTheInventoryIsSortedInstead(TestContext context) {
    Inventory chest = placeWithPartStacks(context, OUT_OF_REACH, Blocks.CHEST.getDefaultState());
    List<ItemStack> chestBefore = snapshotOf(chest);
    ServerPlayerEntity player = playerAt(context, FAR_CORNER);
    stockInventoryWithPartStacks(player);
    lookAt(context, player, OUT_OF_REACH);

    int result = runSort(context, player);

    context.assertTrue(result == 1, "The inventory should have been sorted, returned " + result);
    context.assertTrue(
        matches(chestBefore, snapshotOf(chest)), "A chest out of reach must not be sorted");
    assertInventoryMerged(context, player);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void lookingAtNoContainerSortsTheInventory(TestContext context) {
    ServerPlayerEntity player = playerAt(context, STANDING);
    stockInventoryWithPartStacks(player);
    lookAt(context, player, FLOOR);

    int result = runSort(context, player);

    context.assertTrue(result == 1, "The inventory should have been sorted, returned " + result);
    assertInventoryMerged(context, player);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOpenContainerIsSortedRatherThanTheOneInView(TestContext context) {
    Inventory open = placeWithPartStacks(context, OUT_OF_REACH, Blocks.BARREL.getDefaultState());
    Inventory inView = placeWithPartStacks(context, IN_VIEW, Blocks.CHEST.getDefaultState());
    List<ItemStack> inViewBefore = snapshotOf(inView);
    ServerPlayerEntity player = playerAt(context, STANDING);
    NamedScreenHandlerFactory factory =
        context
            .getBlockState(OUT_OF_REACH)
            .createScreenHandlerFactory(context.getWorld(), context.getAbsolutePos(OUT_OF_REACH));
    player.openHandledScreen(factory);
    lookAt(context, player, IN_VIEW);

    int result = runSort(context, player);

    context.assertTrue(result == 1, "The open barrel should report a sort, returned " + result);
    context.assertTrue(open.getStack(0).getCount() == 50, "The open barrel should be sorted");
    context.assertTrue(
        matches(inViewBefore, snapshotOf(inView)),
        "The chest in view must be left alone while another screen is open");
    finish(context, player);
  }

  private static void assertInventoryMerged(TestContext context, ServerPlayerEntity player) {
    ItemStack first = player.getInventory().getStack(PlayerSlots.FIRST_MAIN_SLOT);
    context.assertTrue(
        first.isOf(Items.COBBLESTONE) && first.getCount() == 50,
        "The inventory's part stacks should have merged, found " + first);
  }

  private static BlockState halfOfADoubleChest(ChestType type) {
    return Blocks.CHEST
        .getDefaultState()
        .with(ChestBlock.FACING, Direction.SOUTH)
        .with(ChestBlock.CHEST_TYPE, type);
  }

  private static Inventory placeWithPartStacks(
      TestContext context, BlockPos where, BlockState state) {
    context.setBlockState(where, state);
    Inventory inventory = context.getBlockEntity(where);
    inventory.setStack(4, new ItemStack(Items.COBBLESTONE, 20));
    inventory.setStack(9, new ItemStack(Items.COBBLESTONE, 30));
    inventory.markDirty();
    return inventory;
  }

  private static void stockInventoryWithPartStacks(ServerPlayerEntity player) {
    player.getInventory().setStack(FIRST_PART_SLOT, new ItemStack(Items.COBBLESTONE, 20));
    player.getInventory().setStack(SECOND_PART_SLOT, new ItemStack(Items.COBBLESTONE, 30));
  }

  private static int runSort(TestContext context, ServerPlayerEntity player) {
    try {
      return context
          .getWorld()
          .getServer()
          .getCommandManager()
          .getDispatcher()
          .execute("tmc sort", player.getCommandSource());
    } catch (CommandSyntaxException rejected) {
      throw new IllegalStateException("/tmc sort did not parse", rejected);
    }
  }

  private static ServerPlayerEntity playerAt(TestContext context, BlockPos where) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(where);
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);
    player.getInventory().clear();
    return player;
  }

  private static void lookAt(TestContext context, ServerPlayerEntity player, BlockPos target) {
    player.lookAt(
        EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(context.getAbsolutePos(target)));
  }

  private static List<ItemStack> nonEmpty(Inventory inventory) {
    List<ItemStack> held = new ArrayList<>();
    for (int slot = 0; slot < inventory.size(); slot++) {
      if (!inventory.getStack(slot).isEmpty()) {
        held.add(inventory.getStack(slot).copy());
      }
    }
    return held;
  }

  private static List<ItemStack> snapshotOf(Inventory inventory) {
    List<ItemStack> copy = new ArrayList<>(inventory.size());
    for (int slot = 0; slot < inventory.size(); slot++) {
      copy.add(inventory.getStack(slot).copy());
    }
    return copy;
  }

  private static boolean matches(List<ItemStack> before, List<ItemStack> after) {
    if (before.size() != after.size()) {
      return false;
    }
    for (int slot = 0; slot < before.size(); slot++) {
      if (!ItemStack.areEqual(before.get(slot), after.get(slot))) {
        return false;
      }
    }
    return true;
  }

  private static void finish(TestContext context, ServerPlayerEntity player) {
    player.closeHandledScreen();
    context.getWorld().getServer().getPlayerManager().remove(player);
    context.complete();
  }
}
