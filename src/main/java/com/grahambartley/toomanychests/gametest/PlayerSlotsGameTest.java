package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.operation.PlayerSlots;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class PlayerSlotsGameTest implements FabricGameTest {
  private static final String BATCH = "player-slots";
  private static final int TICK_LIMIT = 40;

  private static final int FIRST_ARMOUR_SLOT = 36;
  private static final int OFFHAND_SLOT = 40;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyUnlockedSlotOfTheMainInventoryAndHotbarIsOffered(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.unlocked(player, LockedSlots.empty());

    context.assertTrue(
        offered.size() == LockedSlots.SLOT_COUNT,
        "Offered " + offered.size() + " of " + LockedSlots.SLOT_COUNT + " slots");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aLockedSlotIsAbsentRatherThanOfferedAndSkipped(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.getInventory().setStack(4, new ItemStack(Items.DIAMOND, 64));

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.unlocked(player, LockedSlots.of(List.of(4, 21)));

    context.assertTrue(
        offered.size() == LockedSlots.SLOT_COUNT - 2,
        "Offered " + offered.size() + " slots with two of them locked");
    context.assertTrue(
        offered.stream().noneMatch(slot -> slot.getResource().isOf(Items.DIAMOND)),
        "The locked slot's contents were offered anyway");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anIndexInsideTheRangeResolvesToThatSlotOfTheMainInventory(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.getInventory().setStack(11, new ItemStack(Items.EMERALD, 7));

    SingleSlotStorage<ItemVariant> slot = PlayerSlots.slotAt(player, 11);

    context.assertTrue(slot.getResource().isOf(Items.EMERALD), "Resolved the wrong slot");
    context.assertTrue(slot.getAmount() == 7L, "Resolved " + slot.getAmount() + " emeralds");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anIndexOutsideTheRangeIsRefusedRatherThanResolved(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    for (final int outside :
        new int[] {-1, Integer.MIN_VALUE, FIRST_ARMOUR_SLOT, OFFHAND_SLOT, Integer.MAX_VALUE}) {
      context.assertTrue(refuses(player, outside), "Index " + outside + " resolved to a slot");
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void keepingTheHotbarLeavesExactlyTheFirstNineSlotsBehind(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.depositable(player, LockedSlots.empty(), true);

    context.assertTrue(
        offered.size() == LockedSlots.SLOT_COUNT - PlayerSlots.HOTBAR_SLOTS,
        "Offered "
            + offered.size()
            + " of "
            + (LockedSlots.SLOT_COUNT - PlayerSlots.HOTBAR_SLOTS)
            + " slots with the hotbar kept");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void notKeepingTheHotbarOffersTheWholeInventory(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.depositable(player, LockedSlots.empty(), false);

    context.assertTrue(
        offered.size() == LockedSlots.SLOT_COUNT,
        "Offered " + offered.size() + " of " + LockedSlots.SLOT_COUNT + " slots");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void keepingTheHotbarStillHonoursLocksInTheRestOfTheInventory(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.depositable(player, LockedSlots.of(List.of(3, 20)), true);

    context.assertTrue(
        offered.size() == LockedSlots.SLOT_COUNT - PlayerSlots.HOTBAR_SLOTS - 1,
        "A lock inside the hotbar should not be subtracted twice, offered " + offered.size());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theSlotsKeptAreTheHotbarRatherThanTheEndOfTheInventory(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, 1));
    player.getInventory().setStack(PlayerSlots.HOTBAR_SLOTS, new ItemStack(Items.OAK_LOG, 1));

    List<SingleSlotStorage<ItemVariant>> offered =
        PlayerSlots.depositable(player, LockedSlots.empty(), true);

    context.assertTrue(
        offered.stream().noneMatch(slot -> slot.getResource().isOf(Items.DIAMOND)),
        "The hotbar slot was offered anyway");
    context.assertTrue(
        offered.stream().anyMatch(slot -> slot.getResource().isOf(Items.OAK_LOG)),
        "The first slot past the hotbar should still be offered");
    context.complete();
  }

  private static boolean refuses(ServerPlayerEntity player, int index) {
    try {
      PlayerSlots.slotAt(player, index);
      return false;
    } catch (IndexOutOfBoundsException refused) {
      return true;
    }
  }
}
