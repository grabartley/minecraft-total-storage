package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ScreenButtonRowTest {
  private static final ScreenPanel CHEST = new ScreenPanel(120, 40, 176, 168);

  private static final int TITLE_BAND_HEIGHT = 14;
  private static final int FIRST_SLOT_ROW = 17;

  @ParameterizedTest
  @CsvSource({
    "INSIDE_TOP_RIGHT, 0, 1, 274, 43",
    "INSIDE_TOP_RIGHT, 0, 2, 298, 43",
    "INSIDE_TOP_RIGHT, 1, 2, 298, 59",
    "OUTSIDE_TOP_RIGHT, 0, 1, 298, 43",
    "OUTSIDE_TOP_RIGHT, 1, 2, 298, 59",
    "OUTSIDE_BOTTOM_RIGHT, 0, 1, 298, 191",
    "OUTSIDE_BOTTOM_RIGHT, 1, 2, 298, 175"
  })
  void everyAnchorPutsTheButtonWhereItSays(
      ScreenButtonAnchor anchor, int index, int count, int expectedX, int expectedY) {
    assertEquals(
        new ScreenButtonSpot(expectedX, expectedY),
        ScreenButtonRow.at(anchor, CHEST, index, count));
  }

  @Test
  void aLoneButtonSitsInTheBandVanillaLeavesAboveTheSlots() {
    final ScreenButtonSpot spot =
        ScreenButtonRow.at(ScreenButtonAnchor.INSIDE_TOP_RIGHT, CHEST, 0, 1);

    assertEquals(CHEST.x() + CHEST.width() - 8 - ScreenButtonRow.SIZE, spot.x());
    assertEquals(CHEST.y() + 3, spot.y());
    assertTrue(
        spot.y() + ScreenButtonRow.SIZE <= CHEST.y() + FIRST_SLOT_ROW,
        "the band is only "
            + TITLE_BAND_HEIGHT
            + " pixels tall and the button must sit flush in it");
  }

  @Test
  void twoButtonsLeaveTheBandTogetherRatherThanSplittingUp() {
    final ScreenButtonSpot first =
        ScreenButtonRow.at(ScreenButtonAnchor.INSIDE_TOP_RIGHT, CHEST, 0, 2);
    final ScreenButtonSpot second =
        ScreenButtonRow.at(ScreenButtonAnchor.INSIDE_TOP_RIGHT, CHEST, 1, 2);

    assertEquals(first.x(), second.x());
    assertTrue(
        first.x() >= CHEST.x() + CHEST.width(),
        "only one button fits the band, so a pair belongs outside the panel as a column");
    assertEquals(first.y() + ScreenButtonRow.SIZE + ScreenButtonRow.GAP, second.y());
  }

  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void noTwoButtonsEverOverlap(ScreenButtonAnchor anchor) {
    for (int index = 0; index < 3; index++) {
      final ScreenButtonSpot here = ScreenButtonRow.at(anchor, CHEST, index, 4);
      final ScreenButtonSpot next = ScreenButtonRow.at(anchor, CHEST, index + 1, 4);

      assertTrue(
          Math.abs(here.x() - next.x()) >= ScreenButtonRow.SIZE
              || Math.abs(here.y() - next.y()) >= ScreenButtonRow.SIZE,
          anchor + " stacks buttons " + index + " and " + (index + 1) + " on top of each other");
    }
  }

  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void everyAnchorFollowsThePanelRatherThanTheWindow(ScreenButtonAnchor anchor) {
    final ScreenButtonSpot here = ScreenButtonRow.at(anchor, CHEST, 0, 1);
    final ScreenButtonSpot moved =
        ScreenButtonRow.at(
            anchor,
            new ScreenPanel(CHEST.x() + 30, CHEST.y() + 20, CHEST.width(), CHEST.height()),
            0,
            1);

    assertEquals(here.x() + 30, moved.x());
    assertEquals(here.y() + 20, moved.y());
  }

  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void noButtonEverLandsOnASlot(ScreenButtonAnchor anchor) {
    for (int index = 0; index < 3; index++) {
      final ScreenButtonSpot spot = ScreenButtonRow.at(anchor, CHEST, index, 3);
      final boolean insideThePanel =
          spot.x() + ScreenButtonRow.SIZE > CHEST.x() && spot.x() < CHEST.x() + CHEST.width();

      assertTrue(
          !insideThePanel || spot.y() + ScreenButtonRow.SIZE <= CHEST.y() + FIRST_SLOT_ROW,
          anchor + " puts button " + index + " over the slot grid");
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = ScreenButtonAnchor.class,
      names = {"OUTSIDE_TOP_RIGHT", "OUTSIDE_BOTTOM_RIGHT"})
  void anOutsideAnchorKeepsTheButtonBesideThePanelRatherThanOffIt(ScreenButtonAnchor anchor) {
    final ScreenButtonSpot spot = ScreenButtonRow.at(anchor, CHEST, 0, 1);

    assertEquals(CHEST.x() + CHEST.width() + ScreenButtonRow.GAP, spot.x());
    assertTrue(spot.y() >= CHEST.y());
    assertTrue(spot.y() + ScreenButtonRow.SIZE <= CHEST.y() + CHEST.height());
  }
}
