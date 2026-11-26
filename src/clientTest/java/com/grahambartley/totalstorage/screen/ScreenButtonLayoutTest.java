package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScreenButtonLayoutTest {
  private static final ScreenPanel INVENTORY = new ScreenPanel(392, 187, 176, 166);
  private static final int WIDTH = 960;
  private static final int HEIGHT = 540;

  private static List<ScreenButtonSpot> anchored(ScreenButtonAnchor anchor, int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> ScreenButtonRow.at(anchor, INVENTORY, index, count))
        .toList();
  }

  private static List<ScreenButtonSpot> beneath(int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> ScreenButtonRow.beneath(INVENTORY, index, count, HEIGHT))
        .toList();
  }

  static Stream<Arguments> everyAnchorAndCount() {
    return Arrays.stream(ScreenButtonAnchor.values())
        .flatMap(anchor -> Stream.of(Arguments.of(anchor, 1), Arguments.of(anchor, 2)));
  }

  static Stream<Arguments> everyAnchorCountAndScreen() {
    return everyAnchorAndCount()
        .flatMap(
            pair ->
                Stream.of(
                        new int[] {WIDTH, HEIGHT},
                        new int[] {480, 270},
                        new int[] {427, 240},
                        new int[] {260, 240})
                    .flatMap(
                        screen ->
                            IntStream.rangeClosed(1, 9)
                                .mapToObj(
                                    effects ->
                                        Arguments.of(
                                            pair.get()[0],
                                            pair.get()[1],
                                            screen[0],
                                            screen[1],
                                            effects))));
  }

  @ParameterizedTest
  @MethodSource("everyAnchorAndCount")
  void withoutEffectsTheButtonsSitWhereTheAnchorPutsThem(ScreenButtonAnchor anchor, int count) {
    assertEquals(
        anchored(anchor, count),
        new ScreenButtonLayout(anchor, count).spots(INVENTORY, WIDTH, HEIGHT, 0));
  }

  @ParameterizedTest
  @MethodSource("everyAnchorCountAndScreen")
  void noButtonEverSitsUnderTheColumnTheLayoutSteersAround(
      ScreenButtonAnchor anchor, int count, int screenWidth, int screenHeight, int effects) {
    final ScreenPanel panel =
        new ScreenPanel((screenWidth - 176) / 2, (screenHeight - 166) / 2, 176, 166);
    final List<ScreenButtonSpot> spots =
        new ScreenButtonLayout(anchor, count).spots(panel, screenWidth, screenHeight, effects);

    StatusEffectColumn.of(panel, screenWidth, effects)
        .ifPresent(
            column ->
                spots.forEach(
                    spot ->
                        assertFalse(column.covers(spot), anchor + " leaves " + spot + " covered")));
    spots.forEach(
        spot -> {
          assertFalse(spot.y() < 0 || spot.y() + ScreenButtonRow.SIZE > screenHeight);
          assertFalse(spot.x() < 0 || spot.x() + ScreenButtonRow.SIZE > screenWidth);
        });
  }

  @ParameterizedTest
  @CsvSource({
    "OUTSIDE_TOP_RIGHT, 1, 1, true",
    "OUTSIDE_TOP_RIGHT, 2, 1, true",
    "INSIDE_TOP_RIGHT, 2, 1, true",
    "OUTSIDE_BOTTOM_RIGHT, 2, 1, false",
    "OUTSIDE_BOTTOM_RIGHT, 2, 5, true",
    "INSIDE_TOP_RIGHT, 1, 9, false"
  })
  void onlyAButtonTheListWouldCoverStepsBeneathThePanel(
      ScreenButtonAnchor anchor, int count, int effects, boolean stepsBeneath) {
    assertEquals(
        stepsBeneath ? beneath(count) : anchored(anchor, count),
        new ScreenButtonLayout(anchor, count).spots(INVENTORY, WIDTH, HEIGHT, effects));
  }

  @ParameterizedTest
  @EnumSource(
      value = ScreenButtonAnchor.class,
      names = {"OUTSIDE_TOP_RIGHT", "OUTSIDE_BOTTOM_RIGHT"})
  void onceTheButtonsStepBeneathThePanelTheyStayThereAsEffectsComeAndGo(ScreenButtonAnchor anchor) {
    final ScreenButtonLayout layout = new ScreenButtonLayout(anchor, 2);

    layout.spots(INVENTORY, WIDTH, HEIGHT, 6);

    for (final int effects : new int[] {0, 1, 3, 0}) {
      assertEquals(beneath(2), layout.spots(INVENTORY, WIDTH, HEIGHT, effects));
    }
  }

  @ParameterizedTest
  @CsvSource({"0", "1", "4"})
  void aWindowWithNoRoomForTheListLeavesTheButtonsWhereTheyWere(int effects) {
    final ScreenPanel panel = new ScreenPanel(42, 37, 176, 166);

    assertEquals(
        IntStream.range(0, 2)
            .mapToObj(
                index -> ScreenButtonRow.at(ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, panel, index, 2))
            .toList(),
        new ScreenButtonLayout(ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, 2)
            .spots(panel, 249, 240, effects));
  }
}
