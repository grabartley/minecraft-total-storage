package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.lock.LockToggleOutcome;
import com.grahambartley.totalstorage.lock.LockToggleService;
import com.grahambartley.totalstorage.lock.LockedSlots;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class LockToggleServiceGameTest implements FabricGameTest {
  private static final String BATCH = "lock-toggle";
  private static final int TICK_LIMIT = 20;
  private static final int HOTBAR_SLOT = 3;
  private static final int FIRST_ARMOUR_SLOT = 36;
  private static final int OFFHAND_SLOT = 40;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void togglingAnEmptySlotLocksItOnThePlayer(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    LockToggleOutcome outcome = LockToggleService.toggle(player, HOTBAR_SLOT);

    context.assertTrue(
        outcome == LockToggleOutcome.LOCKED, "First toggle should lock but reported " + outcome);
    context.assertTrue(
        LockedSlotsAttachment.get(player).isLocked(HOTBAR_SLOT),
        "The lock should be readable from the player straight away");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void togglingTheSameSlotAgainUnlocksIt(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    LockToggleService.toggle(player, HOTBAR_SLOT);

    LockToggleOutcome outcome = LockToggleService.toggle(player, HOTBAR_SLOT);

    context.assertTrue(
        outcome == LockToggleOutcome.UNLOCKED,
        "Second toggle should unlock but reported " + outcome);
    context.assertTrue(
        LockedSlotsAttachment.get(player).isEmpty(),
        "Unlocking the only locked slot should leave nothing locked");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anArmourSlotIndexIsRejectedAndLeavesExistingLocksAlone(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(2, 7)));

    LockToggleOutcome outcome = LockToggleService.toggle(player, FIRST_ARMOUR_SLOT);

    context.assertTrue(
        outcome == LockToggleOutcome.REJECTED,
        "Armour is outside the lockable range but reported " + outcome);
    context.assertTrue(
        LockedSlotsAttachment.get(player).equals(LockedSlots.of(List.of(2, 7))),
        "A rejected toggle must not disturb the locks the player already had");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOffhandSlotIndexIsRejected(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    LockToggleOutcome outcome = LockToggleService.toggle(player, OFFHAND_SLOT);

    context.assertTrue(
        outcome == LockToggleOutcome.REJECTED,
        "The offhand is outside the lockable range but reported " + outcome);
    context.assertTrue(
        LockedSlotsAttachment.get(player).isEmpty(), "A rejected toggle must lock nothing");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aForgedIndexFarOutsideTheInventoryIsRejected(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    context.assertTrue(
        LockToggleService.toggle(player, Integer.MAX_VALUE) == LockToggleOutcome.REJECTED,
        "A huge index should be rejected");
    context.assertTrue(
        LockToggleService.toggle(player, -1) == LockToggleOutcome.REJECTED,
        "A negative index should be rejected");
    context.assertTrue(
        LockedSlotsAttachment.get(player).isEmpty(), "Forged indices must lock nothing");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void oneLockPerPlayerRatherThanOneLockPerServer(TestContext context) {
    ServerPlayerEntity acting = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity bystander = context.createMockCreativeServerPlayerInWorld();

    LockToggleService.toggle(acting, HOTBAR_SLOT);

    context.assertTrue(
        LockedSlotsAttachment.get(bystander).isEmpty(),
        "One player's lock must not appear on another player");
    disconnectAndComplete(context, acting, bystander);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everySlotTheInventoryOffersCanBeLocked(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    for (int slot = LockedSlots.FIRST_SLOT; slot <= LockedSlots.LAST_SLOT; slot++) {
      context.assertTrue(
          LockToggleService.toggle(player, slot) == LockToggleOutcome.LOCKED,
          "Slot " + slot + " should be lockable");
    }

    context.assertEquals(
        LockedSlotsAttachment.get(player).count(),
        LockedSlots.SLOT_COUNT,
        "Every inventory slot should end up locked");
    disconnectAndComplete(context, player);
  }

  private static void disconnectAndComplete(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    context.complete();
  }
}
