package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ClientPreferences;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ClientPreferenceEditsTest {
  @Test
  void aScreenNobodyTouchedHasNothingToSave() {
    assertFalse(new ClientPreferenceEdits(ClientPreferences.defaults()).isDirty());
  }

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(ClientPreference.SORT_BUTTON_VISIBLE, "false"),
        Arguments.of(ClientPreference.SCREEN_BUTTON_ANCHOR, "outside_bottom_right"),
        Arguments.of(ClientPreference.LOCKED_SLOT_TINT_VISIBLE, "true"));
  }

  @ParameterizedTest
  @MethodSource("changes")
  void changingAnOptionIsSomethingToSave(ClientPreference preference, String choice) {
    final ClientPreferenceEdits edits = new ClientPreferenceEdits(ClientPreferences.defaults());

    assertTrue(edits.set(preference, choice));

    assertTrue(edits.isDirty());
    assertEquals(choice, edits.valueOf(preference));
    assertEquals(choice, preference.valueIn(edits.edited()));
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void cyclingBackToTheSavedValueLeavesNothingToSave(ClientPreference preference) {
    final ClientPreferenceEdits edits = new ClientPreferenceEdits(ClientPreferences.defaults());

    for (final String choice : preference.choices()) {
      edits.set(preference, choice);
    }
    edits.set(preference, preference.defaultValue());

    assertFalse(edits.isDirty());
  }

  @ParameterizedTest
  @MethodSource("changes")
  void aRefusedValueChangesNothing(ClientPreference preference, String ignored) {
    final ClientPreferenceEdits edits = new ClientPreferenceEdits(ClientPreferences.defaults());

    assertFalse(edits.set(preference, "nonsense"));

    assertFalse(edits.isDirty());
    assertEquals(preference.defaultValue(), edits.valueOf(preference));
  }

  @Test
  void theDraftStartsFromThePlayersSavedOptions() {
    final ClientPreferences saved =
        new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, true);

    final ClientPreferenceEdits edits = new ClientPreferenceEdits(saved);

    assertEquals(saved, edits.edited());
    assertEquals("outside_top_right", edits.valueOf(ClientPreference.SCREEN_BUTTON_ANCHOR));
  }
}
