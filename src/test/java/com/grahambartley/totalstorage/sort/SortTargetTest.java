package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SortTargetTest {
  private static Inventory anInventory() {
    return mock(Inventory.class);
  }

  @Test
  void aTargetReportsHowManySlotsItCovers() {
    assertEquals(3, new SortTarget(anInventory(), List.of(0, 1, 2)).slotCount());
  }

  @Test
  void aTargetReadsItsCapacityFromTheInventoryItCovers() {
    Inventory inventory = anInventory();
    when(inventory.getMaxCountPerStack()).thenReturn(16);

    assertEquals(16, new SortTarget(inventory, List.of(0, 1)).slotCapacity());
  }

  @Test
  void theIndicesKeepTheOrderTheArrangementIsWrittenBackIn() {
    assertEquals(List.of(4, 0, 9), new SortTarget(anInventory(), List.of(4, 0, 9)).indices());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void aTargetTooSmallToRearrangeIsRejected(int slots) {
    List<Integer> indices = new ArrayList<>();
    for (int index = 0; index < slots; index++) {
      indices.add(index);
    }
    Inventory inventory = anInventory();

    assertThrows(IllegalArgumentException.class, () -> new SortTarget(inventory, indices));
  }

  @Test
  void aTargetWithoutAnInventoryIsRejected() {
    assertThrows(NullPointerException.class, () -> new SortTarget(null, List.of(0, 1)));
  }

  @Test
  void aTargetWithoutIndicesIsRejected() {
    Inventory inventory = anInventory();

    assertThrows(NullPointerException.class, () -> new SortTarget(inventory, null));
  }

  @Test
  void theIndicesAreNotAffectedByLaterChangesToTheListPassedIn() {
    List<Integer> mutable = new ArrayList<>(List.of(0, 1));
    SortTarget target = new SortTarget(anInventory(), mutable);
    mutable.add(2);

    assertEquals(List.of(0, 1), target.indices());
  }

  @Test
  void theIndicesCannotBeChangedByCallers() {
    SortTarget target = new SortTarget(anInventory(), List.of(0, 1));

    assertThrows(UnsupportedOperationException.class, () -> target.indices().add(2));
  }
}
