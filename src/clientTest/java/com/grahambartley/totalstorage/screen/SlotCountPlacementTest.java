package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlotCountPlacementTest {
  @ParameterizedTest
  @CsvSource({"8, 8, 6", "8, 8, 12", "44, 17, 12", "0, 0, 1"})
  void aTwoDigitLabelSitsExactlyWhereVanillaDrawsAStackCount(int itemX, int itemY, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, width, 9);

    assertEquals(1.0F, placement.scale());
    assertEquals(itemX + 19 - 2 - width, placement.x());
    assertEquals(itemY + 6 + 3, placement.y());
  }

  @ParameterizedTest
  @CsvSource({"8, 8, 13", "8, 8, 18", "8, 8, 26", "44, 17, 25", "131, 77, 32"})
  void aWiderLabelDrawsAtHalfSizeAndKeepsVanillasBottomRightCorner(
      int itemX, int itemY, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, width, 9);

    assertEquals(0.5F, placement.scale());
    assertEquals(itemX + 17, (placement.x() + width) * placement.scale(), 0.5F);
    assertEquals(itemY + 18, (placement.y() + 9) * placement.scale(), 0.5F);
  }
}
