package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ClientPreferenceTest {
  private static final ClientPreferences DEFAULTS = ClientPreferences.defaults();

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(
            ClientPreference.SORT_BUTTON_VISIBLE,
            "false",
            new ClientPreferences(false, ScreenButtonAnchor.INSIDE_TOP_RIGHT, false)),
        Arguments.of(
            ClientPreference.SCREEN_BUTTON_ANCHOR,
            "outside_bottom_right",
            new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, false)),
        Arguments.of(
            ClientPreference.SCREEN_BUTTON_ANCHOR,
            " Outside_Top_Right ",
            new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, false)),
        Arguments.of(
            ClientPreference.LOCKED_SLOT_TINT_VISIBLE,
            "TRUE",
            new ClientPreferences(true, ScreenButtonAnchor.INSIDE_TOP_RIGHT, true)));
  }

  @ParameterizedTest
  @MethodSource("changes")
  void aChoiceChangesOnlyItsOwnOption(
      ClientPreference preference, String choice, ClientPreferences expected) {
    assertEquals(expected, preference.withValue(DEFAULTS, choice).orElseThrow());
  }

  static Stream<Arguments> refusals() {
    return Stream.of(
        Arguments.of(ClientPreference.SORT_BUTTON_VISIBLE, "yes"),
        Arguments.of(ClientPreference.SORT_BUTTON_VISIBLE, null),
        Arguments.of(ClientPreference.SCREEN_BUTTON_ANCHOR, "left"),
        Arguments.of(ClientPreference.SCREEN_BUTTON_ANCHOR, ""),
        Arguments.of(ClientPreference.LOCKED_SLOT_TINT_VISIBLE, "1"));
  }

  @ParameterizedTest
  @MethodSource("refusals")
  void aValueTheOptionDoesNotTakeIsRefused(ClientPreference preference, String choice) {
    assertTrue(preference.withValue(DEFAULTS, choice).isEmpty());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void everyChoiceRoundTripsThroughTheOption(ClientPreference preference) {
    for (final String choice : preference.choices()) {
      final ClientPreferences changed = preference.withValue(DEFAULTS, choice).orElseThrow();

      assertEquals(choice, preference.valueIn(changed));
    }
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void theDefaultIsOneOfTheChoices(ClientPreference preference) {
    assertTrue(preference.choices().contains(preference.defaultValue()));
  }

  static Stream<Arguments> defaults() {
    return Stream.of(
        Arguments.of(ClientPreference.SORT_BUTTON_VISIBLE, "true"),
        Arguments.of(ClientPreference.SCREEN_BUTTON_ANCHOR, "inside_top_right"),
        Arguments.of(ClientPreference.LOCKED_SLOT_TINT_VISIBLE, "false"));
  }

  @ParameterizedTest
  @MethodSource("defaults")
  void theDefaultMatchesTheShippedPreferences(ClientPreference preference, String expected) {
    assertEquals(expected, preference.defaultValue());
  }

  static Stream<Arguments> keys() {
    return Stream.of(
        Arguments.of(ClientPreference.SORT_BUTTON_VISIBLE, "sortButtonVisible"),
        Arguments.of(ClientPreference.SCREEN_BUTTON_ANCHOR, "screenButtonAnchor"),
        Arguments.of(ClientPreference.LOCKED_SLOT_TINT_VISIBLE, "lockedSlotTintVisible"));
  }

  @ParameterizedTest
  @MethodSource("keys")
  void theKeyIsTheNameInTheClientFile(ClientPreference preference, String key) {
    assertEquals(key, preference.key());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void theAnchorOffersEveryPositionAndSwitchesOfferBothStates(ClientPreference preference) {
    final List<String> expected =
        preference == ClientPreference.SCREEN_BUTTON_ANCHOR
            ? List.of("inside_top_right", "outside_top_right", "outside_bottom_right")
            : List.of("true", "false");

    assertEquals(expected, preference.choices());
  }
}
