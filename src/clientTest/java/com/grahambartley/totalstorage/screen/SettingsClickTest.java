package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SettingsClickTest {
  private static final int LEFT = GridClick.LEFT_BUTTON;
  private static final int RIGHT = GridClick.RIGHT_BUTTON;
  private static final ContainerRowTarget BODY = ContainerRowTarget.BODY;
  private static final ContainerRowTarget ROLE = ContainerRowTarget.ROLE;
  private static final boolean SHIFT = true;
  private static final boolean PLAIN = false;
  private static final boolean MAY = true;
  private static final boolean MAY_NOT = false;
  private static final boolean FILTERED = true;
  private static final boolean UNFILTERED = false;

  static Stream<Arguments> clicks() {
    return Stream.of(
        Arguments.of(LEFT, ROLE, PLAIN, MAY, UNFILTERED, SettingsAction.CYCLE_FORWARD),
        Arguments.of(RIGHT, ROLE, PLAIN, MAY, UNFILTERED, SettingsAction.CYCLE_BACK),
        Arguments.of(LEFT, ROLE, PLAIN, MAY_NOT, UNFILTERED, SettingsAction.REFUSED),
        Arguments.of(RIGHT, ROLE, PLAIN, MAY_NOT, UNFILTERED, SettingsAction.REFUSED),
        Arguments.of(LEFT, BODY, PLAIN, MAY, UNFILTERED, SettingsAction.LOCATE),
        Arguments.of(LEFT, BODY, PLAIN, MAY_NOT, UNFILTERED, SettingsAction.LOCATE),
        Arguments.of(RIGHT, BODY, PLAIN, MAY, UNFILTERED, SettingsAction.NONE),
        Arguments.of(RIGHT, BODY, PLAIN, MAY_NOT, UNFILTERED, SettingsAction.NONE),
        Arguments.of(RIGHT, BODY, SHIFT, MAY, UNFILTERED, SettingsAction.RENAME),
        Arguments.of(RIGHT, ROLE, SHIFT, MAY, FILTERED, SettingsAction.RENAME),
        Arguments.of(LEFT, BODY, SHIFT, MAY, FILTERED, SettingsAction.CHOOSE_TAGS),
        Arguments.of(LEFT, ROLE, SHIFT, MAY, FILTERED, SettingsAction.CHOOSE_TAGS),
        Arguments.of(LEFT, BODY, SHIFT, MAY, UNFILTERED, SettingsAction.REFUSED),
        Arguments.of(LEFT, BODY, SHIFT, MAY_NOT, FILTERED, SettingsAction.REFUSED),
        Arguments.of(RIGHT, ROLE, SHIFT, MAY_NOT, FILTERED, SettingsAction.REFUSED));
  }

  @ParameterizedTest(name = "button {0}, target {1}, shift {2}, may {3}, filtered {4}")
  @MethodSource("clicks")
  void aClickMeansWhatItsPlaceOnTheRowSays(
      int button,
      ContainerRowTarget target,
      boolean shiftDown,
      boolean mayAdminister,
      boolean filtered,
      SettingsAction action) {
    assertEquals(action, SettingsClick.of(button, target, shiftDown, mayAdminister, filtered));
  }

  @ParameterizedTest
  @EnumSource(ContainerRowTarget.class)
  void aButtonWithNoMeaningIsHandedBack(ContainerRowTarget target) {
    assertEquals(
        SettingsAction.NONE,
        SettingsClick.of(GridClick.MIDDLE_BUTTON, target, SHIFT, MAY, FILTERED));
  }

  @ParameterizedTest(name = "button {0}")
  @MethodSource("buttons")
  void aClickOnNoRowIsHandedBack(int button) {
    assertEquals(
        SettingsAction.NONE,
        SettingsClick.of(button, ContainerRowTarget.NONE, PLAIN, MAY, FILTERED));
  }

  static Stream<Integer> buttons() {
    return Stream.of(LEFT, RIGHT);
  }
}
