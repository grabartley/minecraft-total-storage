package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Proves the locked-slot attachment behaves the way a real player session needs it to. Persistence,
 * respawn transfer, and sync targeting are all wiring that only exists once a server is running, so
 * none of it can be shown off-game.
 */
public final class LockedSlotsAttachmentGameTest implements FabricGameTest {
  private static final String BATCH = "locked-slots";
  private static final int TICK_LIMIT = 20;
  private static final int LOCKED_SLOT = 3;
  private static final List<Integer> SPREAD = List.of(0, LOCKED_SLOT, 35);

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFreshPlayerHasNoLockedSlots(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    LockedSlots locks = LockedSlotsAttachment.get(player);

    context.assertTrue(locks.isEmpty(), "A player who has never locked anything should read empty");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void lockedSlotsSurviveTheNbtRoundTripThatSavesAndReloadsAWorld(TestContext context) {
    ServerPlayerEntity saved = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity reloaded = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(saved, LockedSlots.of(SPREAD));

    reloaded.readNbt(saved.writeNbt(new NbtCompound()));

    LockedSlots restored = LockedSlotsAttachment.get(reloaded);
    context.assertTrue(
        restored.equals(LockedSlots.of(SPREAD)),
        "Locks should come back from NBT but read " + restored);
    disconnectAndComplete(context, saved, reloaded);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPlayerWithNoLocksWritesNothingToReadBack(TestContext context) {
    ServerPlayerEntity saved = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity reloaded = context.createMockCreativeServerPlayerInWorld();

    reloaded.readNbt(saved.writeNbt(new NbtCompound()));

    context.assertTrue(
        LockedSlotsAttachment.get(reloaded).isEmpty(),
        "A player who locked nothing should still read empty after a reload");
    disconnectAndComplete(context, saved, reloaded);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void lockedSlotsFollowThePlayerThroughDeath(TestContext context) {
    ServerPlayerEntity died = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity respawned = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(died, LockedSlots.of(List.of(LOCKED_SLOT)));

    respawnAfterDeath(died, respawned);

    context.assertTrue(
        LockedSlotsAttachment.get(respawned).isLocked(LOCKED_SLOT),
        "Slot " + LOCKED_SLOT + " should still be locked after respawning");
    disconnectAndComplete(context, died, respawned);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void deathEmptiesALockedSlotWithoutUnlockingIt(TestContext context) {
    ServerPlayerEntity died = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity respawned = context.createMockCreativeServerPlayerInWorld();
    died.getInventory().setStack(LOCKED_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    LockedSlotsAttachment.set(died, LockedSlots.of(List.of(LOCKED_SLOT)));

    respawnAfterDeath(died, respawned);

    context.assertTrue(
        respawned.getInventory().getStack(LOCKED_SLOT).isEmpty(),
        "A lock preserves layout, not contents, so the slot should come back empty");
    context.assertTrue(
        LockedSlotsAttachment.get(respawned).isLocked(LOCKED_SLOT),
        "The empty slot should still be locked so the layout is preserved");
    disconnectAndComplete(context, died, respawned);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theLockedSetIsOfferedToItsOwnerOnly(TestContext context) {
    ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity bystander = context.createMockCreativeServerPlayerInWorld();
    LockedSlotsAttachment.set(owner, LockedSlots.of(List.of(LOCKED_SLOT)));

    context.assertTrue(
        LockedSlotsAttachment.SYNC_PREDICATE.test(owner, owner),
        "The owning player should receive their own locks");
    context.assertFalse(
        LockedSlotsAttachment.SYNC_PREDICATE.test(owner, bystander),
        "Another player should never receive somebody else's locks");
    disconnectAndComplete(context, owner, bystander);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onePlayersLocksDoNotLeakOntoAnother(TestContext context) {
    ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();

    LockedSlotsAttachment.set(first, LockedSlots.of(List.of(LOCKED_SLOT)));

    context.assertTrue(
        LockedSlotsAttachment.get(second).isEmpty(),
        "Locking a slot for one player should leave every other player untouched");
    disconnectAndComplete(context, first, second);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void togglingASlotStoresTheNewSetOnThePlayer(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    LockedSlotsAttachment.toggle(player, LOCKED_SLOT);
    boolean lockedAfterFirstToggle = LockedSlotsAttachment.get(player).isLocked(LOCKED_SLOT);
    LockedSlotsAttachment.toggle(player, LOCKED_SLOT);

    context.assertTrue(lockedAfterFirstToggle, "The first toggle should lock the slot");
    context.assertTrue(
        LockedSlotsAttachment.get(player).isEmpty(), "The second toggle should unlock it again");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aSlotBeyondTheMainInventoryIsNeverStoredOnThePlayer(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    LockedSlotsAttachment.toggle(player, LockedSlots.LAST_SLOT + 1);

    context.assertTrue(
        LockedSlotsAttachment.get(player).isEmpty(),
        "Armour and offhand slots are out of range and should never be stored");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theLockableRangeCoversTheMainInventoryAndNothingElse(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    for (int slot = LockedSlots.FIRST_SLOT; slot <= LockedSlots.LAST_SLOT; slot++) {
      player.getInventory().setStack(slot, new ItemStack(Items.STONE));
    }

    context.assertTrue(
        player.getInventory().main.stream().noneMatch(ItemStack::isEmpty),
        "Every lockable index should land in the main inventory, leaving no gap");
    context.assertTrue(
        player.getInventory().armor.stream().allMatch(ItemStack::isEmpty),
        "The lockable range should stop before armour");
    context.assertTrue(
        player.getInventory().offHand.stream().allMatch(ItemStack::isEmpty),
        "The lockable range should stop before the offhand");
    disconnectAndComplete(context, player);
  }

  private static void respawnAfterDeath(ServerPlayerEntity died, ServerPlayerEntity respawned) {
    ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(died, respawned, false);
  }

  /**
   * A mock player left in the player manager holds its chunk tickets for the rest of the run, and
   * batches running in other structures then measure a world this test loaded.
   */
  private static void disconnectAndComplete(TestContext context, ServerPlayerEntity... players) {
    PlayerManager manager = context.getWorld().getServer().getPlayerManager();
    for (ServerPlayerEntity player : players) {
      manager.remove(player);
    }
    context.complete();
  }
}
