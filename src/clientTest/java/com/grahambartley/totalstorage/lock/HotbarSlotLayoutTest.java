package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HotbarSlotLayoutTest {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 360;

  @Test
  void theFirstSlotSitsAtTheLeftEndOfTheBar() {
    assertEquals(WIDTH / 2 - 90 + 2, HotbarSlotLayout.itemX(WIDTH, 0));
  }

  @Test
  void theLastSlotSitsAtTheRightEndOfTheBar() {
    assertEquals(WIDTH / 2 - 90 + 8 * 20 + 2, HotbarSlotLayout.itemX(WIDTH, 8));
  }

  @Test
  void everySlotIsTwentyPixelsAfterTheOneBeforeIt() {
    IntStream.range(1, HotbarSlotLayout.SLOTS)
        .forEach(
            slot ->
                assertEquals(
                    20,
                    HotbarSlotLayout.itemX(WIDTH, slot) - HotbarSlotLayout.itemX(WIDTH, slot - 1)));
  }

  @Test
  void theBarSitsAboveTheBottomOfTheScreen() {
    assertEquals(HEIGHT - 19, HotbarSlotLayout.itemY(HEIGHT));
  }

  @ParameterizedTest
  @ValueSource(ints = {320, 640, 1920})
  void theBarStaysCentredAtEveryGuiScale(int width) {
    int first = HotbarSlotLayout.itemX(width, 0);
    int last = HotbarSlotLayout.itemX(width, 8) + 16;

    assertTrue(
        Math.abs((first + last) / 2 - width / 2) <= 1,
        "The bar should stay centred but ran " + first + " to " + last + " across " + width);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, HotbarSlotLayout.SLOTS})
  void thereIsNoSlotOutsideTheBar(int slot) {
    assertThrows(IndexOutOfBoundsException.class, () -> HotbarSlotLayout.itemX(WIDTH, slot));
  }
}
