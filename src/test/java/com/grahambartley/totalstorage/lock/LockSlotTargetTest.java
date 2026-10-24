package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LockSlotTargetTest {
  private static final Inventory PLAYER_INVENTORY = mock(PlayerInventory.class);
  private static final Inventory CHEST = mock(SimpleInventory.class);

  @ParameterizedTest
  @ValueSource(ints = {0, 8, 9, 35})
  void aMainInventorySlotResolvesToItsOwnIndex(int index) {
    Slot slot = new Slot(PLAYER_INVENTORY, index, 0, 0);

    assertEquals(index, LockSlotTarget.lockableIndex(slot));
    assertTrue(LockSlotTarget.isLockable(slot));
  }

  @ParameterizedTest
  @ValueSource(ints = {36, 37, 38, 39, 40})
  void armourAndOffhandSlotsAreNotLockable(int index) {
    Slot slot = new Slot(PLAYER_INVENTORY, index, 0, 0);

    assertEquals(LockSlotTarget.NONE, LockSlotTarget.lockableIndex(slot));
    assertFalse(LockSlotTarget.isLockable(slot));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 9, 26})
  void aContainerSlotIsNotLockableEvenAtALockableIndex(int index) {
    Slot slot = new Slot(CHEST, index, 0, 0);

    assertEquals(LockSlotTarget.NONE, LockSlotTarget.lockableIndex(slot));
  }

  @Test
  void anAbsentSlotIsNotLockable() {
    assertEquals(LockSlotTarget.NONE, LockSlotTarget.lockableIndex(null));
    assertFalse(LockSlotTarget.isLockable(null));
  }
}
