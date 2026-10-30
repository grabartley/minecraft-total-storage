package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SettingsClickTest {
  private static final boolean ON_ROW = true;
  private static final boolean OFF_ROW = false;
  private static final boolean ON_ROLE = true;
  private static final boolean ON_BODY = false;
  private static final boolean MAY = true;
  private static final boolean MAY_NOT = false;

  static Stream<Arguments> clicks() {
    return Stream.of(
        Arguments.of(GridClick.LEFT_BUTTON, ON_ROW, ON_ROLE, MAY, SettingsAction.CYCLE_FORWARD),
        Arguments.of(GridClick.RIGHT_BUTTON, ON_ROW, ON_ROLE, MAY, SettingsAction.CYCLE_BACK),
        Arguments.of(GridClick.LEFT_BUTTON, ON_ROW, ON_ROLE, MAY_NOT, SettingsAction.REFUSED),
        Arguments.of(GridClick.RIGHT_BUTTON, ON_ROW, ON_ROLE, MAY_NOT, SettingsAction.REFUSED),
        Arguments.of(GridClick.LEFT_BUTTON, ON_ROW, ON_BODY, MAY, SettingsAction.LOCATE),
        Arguments.of(GridClick.LEFT_BUTTON, ON_ROW, ON_BODY, MAY_NOT, SettingsAction.LOCATE),
        Arguments.of(GridClick.RIGHT_BUTTON, ON_ROW, ON_BODY, MAY, SettingsAction.NONE),
        Arguments.of(GridClick.RIGHT_BUTTON, ON_ROW, ON_BODY, MAY_NOT, SettingsAction.NONE),
        Arguments.of(GridClick.LEFT_BUTTON, OFF_ROW, ON_ROLE, MAY, SettingsAction.NONE),
        Arguments.of(GridClick.RIGHT_BUTTON, OFF_ROW, ON_BODY, MAY, SettingsAction.NONE),
        Arguments.of(GridClick.MIDDLE_BUTTON, ON_ROW, ON_ROLE, MAY, SettingsAction.NONE),
        Arguments.of(GridClick.MIDDLE_BUTTON, ON_ROW, ON_ROLE, MAY_NOT, SettingsAction.NONE),
        Arguments.of(GridClick.MIDDLE_BUTTON, ON_ROW, ON_BODY, MAY, SettingsAction.NONE),
        Arguments.of(7, ON_ROW, ON_ROLE, MAY, SettingsAction.NONE));
  }

  @ParameterizedTest(name = "button {0}, row {1}, role {2}, may {3}")
  @MethodSource("clicks")
  void aClickMeansWhatItsPlaceOnTheRowSays(
      int button, boolean overRow, boolean overRole, boolean mayAdminister, SettingsAction action) {
    assertEquals(action, SettingsClick.of(button, overRow, overRole, mayAdminister));
  }
}
