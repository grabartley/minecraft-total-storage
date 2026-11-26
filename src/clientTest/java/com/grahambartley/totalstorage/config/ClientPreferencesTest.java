package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClientPreferencesTest {
  @Test
  void aFreshInstallShowsTheSortButtonButLeavesLockedSlotsUntinted() {
    final ClientPreferences defaults = ClientPreferences.defaults();

    assertTrue(defaults.sortButtonVisible());
    assertFalse(defaults.lockedSlotTintVisible());
    assertEquals(ScreenButtonAnchor.INSIDE_TOP_RIGHT, defaults.screenButtonAnchor());
  }

  @Test
  void anAnchorNobodyChosenFallsBackToTheDefault() {
    assertEquals(
        ClientPreferences.DEFAULT_SCREEN_BUTTON_ANCHOR,
        new ClientPreferences(true, null, true).screenButtonAnchor());
  }

  private static final ClientPreferences START =
      new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, false);

  static Stream<Arguments> withers() {
    return Stream.of(
        Arguments.of(
            (UnaryOperator<ClientPreferences>) p -> p.withSortButtonVisible(false),
            new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, false)),
        Arguments.of(
            (UnaryOperator<ClientPreferences>)
                p -> p.withScreenButtonAnchor(ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT),
            new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, false)),
        Arguments.of(
            (UnaryOperator<ClientPreferences>) p -> p.withLockedSlotTintVisible(true),
            new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, true)),
        Arguments.of(
            (UnaryOperator<ClientPreferences>) p -> p.withScreenButtonAnchor(null),
            new ClientPreferences(true, ScreenButtonAnchor.INSIDE_TOP_RIGHT, false)));
  }

  @ParameterizedTest
  @MethodSource("withers")
  void eachWitherChangesOnlyItsOwnField(
      UnaryOperator<ClientPreferences> change, ClientPreferences expected) {
    assertEquals(expected, change.apply(START));
  }
}
