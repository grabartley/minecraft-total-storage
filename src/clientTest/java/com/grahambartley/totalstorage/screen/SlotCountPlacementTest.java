package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlotCountPlacementTest {
  @ParameterizedTest
  @CsvSource({"8, 8, 1, 6", "8, 8, 64, 12", "44, 17, 99, 12", "0, 0, 7, 6", "44, 17, 99, 14"})
  void aCountUpToNinetyNineSitsExactlyWhereVanillaDrawsAStackCount(
      int itemX, int itemY, long count, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, count, width, 9);

    assertEquals(1.0F, placement.scale());
    assertEquals(itemX + 19 - 2 - width, placement.x());
    assertEquals(itemY + 6 + 3, placement.y());
    assertFalse(placement.outlined());
  }

  @ParameterizedTest
  @CsvSource({
    "8, 8, 100, 18",
    "8, 8, 1280, 26",
    "8, 8, 9999, 26",
    "44, 17, 12399, 25",
    "131, 77, 1000000, 20",
    "44, 17, 100, 11"
  })
  void aBiggerCountDrawsAtHalfSizeWithAnOutlineAndKeepsVanillasBottomRightCorner(
      int itemX, int itemY, long count, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, count, width, 9);

    assertEquals(0.5F, placement.scale());
    assertTrue(placement.outlined());
    assertEquals(itemX + 17, (placement.x() + width) * placement.scale(), 0.5F);
    assertEquals(itemY + 18, (placement.y() + 9) * placement.scale(), 0.5F);
  }
}
