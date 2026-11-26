package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlotCountPlacementTest {
  @ParameterizedTest
  @CsvSource({"8, 8, 5", "8, 8, 11", "44, 17, 16", "0, 0, 1"})
  void aLabelThatFitsSitsExactlyWhereVanillaDrawsAStackCount(int itemX, int itemY, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, width);

    assertEquals(1.0F, placement.scale());
    assertEquals(itemX + 19 - 2 - width, placement.x());
    assertEquals(itemY + 6 + 3, placement.y());
  }

  @ParameterizedTest
  @CsvSource({"8, 8, 17", "8, 8, 23", "8, 8, 32", "44, 17, 23", "131, 77, 29"})
  void aWideLabelShrinksToTheItemWidthAndKeepsVanillasBottomRightCorner(
      int itemX, int itemY, int width) {
    final SlotCountPlacement placement = SlotCountPlacement.of(itemX, itemY, width);

    assertEquals(16.0F / width, placement.scale(), 1.0e-6F);
    assertEquals(itemX + 17, (placement.x() + width) * placement.scale(), 1.0F);
    assertEquals(itemY + 18, (placement.y() + 9) * placement.scale(), 1.0F);
  }
}
