package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScreenButtonsTest {
  @Test
  void aContainerScreenCarriesOnlyTheSortButton() {
    assertEquals(List.of(ScreenButton.SORT), ScreenButtons.of(false, true));
  }

  @Test
  void thePlayerInventoryCarriesQuickStackAheadOfSort() {
    assertEquals(
        List.of(ScreenButton.QUICK_STACK, ScreenButton.SORT), ScreenButtons.of(true, true));
  }

  @Test
  void aScreenThatCannotBeSortedStillQuickStacksFromTheInventory() {
    assertEquals(List.of(ScreenButton.QUICK_STACK), ScreenButtons.of(true, false));
  }

  @Test
  void aScreenWhoseSlotsMeanSomethingCarriesNothing() {
    assertTrue(ScreenButtons.of(false, false).isEmpty());
  }

  @Test
  void theRowIsFixedOnceTheScreenIsBuilt() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> ScreenButtons.of(true, true).add(ScreenButton.SORT));
  }
}
