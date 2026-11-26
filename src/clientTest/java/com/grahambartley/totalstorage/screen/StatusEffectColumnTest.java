package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusEffectColumnTest {
  private static final ScreenPanel INVENTORY = new ScreenPanel(392, 187, 176, 166);

  @ParameterizedTest
  @CsvSource({
    "960, 1, 570, 187, 120, 32",
    "960, 2, 570, 187, 120, 65",
    "960, 5, 570, 187, 120, 164",
    "960, 6, 570, 187, 120, 162",
    "960, 9, 570, 187, 120, 160",
    "689, 1, 570, 187, 32, 32",
    "690, 1, 570, 187, 120, 32",
    "602, 3, 570, 187, 32, 98"
  })
  void theColumnMatchesWhereVanillaDrawsTheEffectList(
      int screenWidth, int effects, int x, int y, int width, int height) {
    assertEquals(
        Optional.of(new StatusEffectColumn(x, y, width, height)),
        StatusEffectColumn.of(INVENTORY, screenWidth, effects));
  }

  @ParameterizedTest
  @CsvSource({"960, 0", "601, 1", "570, 4"})
  void thereIsNoColumnWithoutEffectsOrWithoutRoomForOne(int screenWidth, int effects) {
    assertEquals(Optional.empty(), StatusEffectColumn.of(INVENTORY, screenWidth, effects));
  }

  @ParameterizedTest
  @CsvSource({
    "570, 187, true",
    "556, 187, false",
    "557, 187, true",
    "689, 200, true",
    "690, 200, false",
    "600, 218, true",
    "600, 219, false",
    "600, 173, false",
    "600, 174, true"
  })
  void aButtonIsCoveredWhenAnyOfItsPixelsSitsUnderTheColumn(int x, int y, boolean covered) {
    final StatusEffectColumn column = new StatusEffectColumn(570, 187, 120, 32);

    assertEquals(covered, column.covers(new ScreenButtonSpot(x, y)));
  }

  @ParameterizedTest
  @CsvSource({"1", "5", "6", "20"})
  void theColumnNeverRunsPastTheBottomOfAnInventoryPanel(int effects) {
    final StatusEffectColumn column = StatusEffectColumn.of(INVENTORY, 960, effects).orElseThrow();

    assertTrue(column.y() + column.height() <= INVENTORY.y() + INVENTORY.height());
    assertFalse(column.x() < INVENTORY.x() + INVENTORY.width());
  }
}
