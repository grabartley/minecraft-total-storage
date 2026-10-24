package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ScreenButtonBandTest {
  private static final ScreenPanel CHEST = new ScreenPanel(120, 40, 176, 168);

  private static List<ScreenButtonSpot> vanillaChestSlots() {
    return IntStream.range(0, 54)
        .mapToObj(
            index ->
                new ScreenButtonSpot(
                    CHEST.x() + 7 + (index % 9) * 18, CHEST.y() + 17 + (index / 9) * 18))
        .toList();
  }

  @Test
  void aVanillaChestLeavesTheBandClear() {
    assertTrue(ScreenButtonBand.isClear(CHEST, vanillaChestSlots()));
  }

  @Test
  void aScreenWithNoSlotsAtAllLeavesTheBandClear() {
    assertTrue(ScreenButtonBand.isClear(CHEST, List.of()));
  }

  @Test
  void aSlotRaisedIntoTheBandTakesItAway() {
    assertFalse(
        ScreenButtonBand.isClear(
            CHEST, List.of(new ScreenButtonSpot(CHEST.x() + 151, CHEST.y() + 3))));
  }

  @Test
  void aSlotSharingOnlyTheBandsColumnsIsNotInTheWay() {
    assertTrue(
        ScreenButtonBand.isClear(
            CHEST, List.of(new ScreenButtonSpot(CHEST.x() + 151, CHEST.y() + 17))));
  }

  @Test
  void aSlotSharingOnlyTheBandsRowsIsNotInTheWay() {
    assertTrue(
        ScreenButtonBand.isClear(
            CHEST, List.of(new ScreenButtonSpot(CHEST.x() + 7, CHEST.y() + 3))));
  }

  @Test
  void aSlotOverlappingTheBandByOnePixelTakesItAway() {
    assertFalse(
        ScreenButtonBand.isClear(
            CHEST, List.of(new ScreenButtonSpot(CHEST.x() + 151, CHEST.y() + 16))));
  }
}
