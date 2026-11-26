package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ClientPreference;
import com.grahambartley.totalstorage.config.ClientPreferences;
import com.grahambartley.totalstorage.config.ConfigOption;
import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.ConfigSettings;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigDraftTest {
  private static final ConfigSetting RADIUS =
      ConfigSettings.byName("maxTerminalRadius").orElseThrow();
  private static final ConfigSetting SORT_BUTTON =
      ConfigSettings.byName("sortButtonEnabled").orElseThrow();

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(TotalStorageConfig.defaults(), RADIUS, "32"),
        Arguments.of(TotalStorageConfig.defaults(), SORT_BUTTON, "false"),
        Arguments.of(ClientPreferences.defaults(), ClientPreference.SORT_BUTTON_VISIBLE, "false"),
        Arguments.of(
            ClientPreferences.defaults(),
            ClientPreference.SCREEN_BUTTON_ANCHOR,
            "outside_bottom_right"),
        Arguments.of(
            ClientPreferences.defaults(), ClientPreference.LOCKED_SLOT_TINT_VISIBLE, "true"));
  }

  static Stream<Arguments> refusals() {
    return Stream.of(
        Arguments.of(TotalStorageConfig.defaults(), RADIUS, "999"),
        Arguments.of(ClientPreferences.defaults(), ClientPreference.SORT_BUTTON_VISIBLE, "maybe"),
        Arguments.of(ClientPreferences.defaults(), ClientPreference.SCREEN_BUTTON_ANCHOR, "left"));
  }

  @ParameterizedTest
  @MethodSource("changes")
  <S> void aScreenNobodyTouchedHasNothingToSubmit(
      S baseline, ConfigOption<S> option, String ignored) {
    final ConfigDraft<S> draft = new ConfigDraft<>(baseline);

    assertFalse(draft.isDirty());
    assertEquals(option.valueIn(baseline), draft.valueOf(option));
  }

  @ParameterizedTest
  @MethodSource("changes")
  <S> void changingAnOptionIsSomethingToSubmit(S baseline, ConfigOption<S> option, String value) {
    final ConfigDraft<S> draft = new ConfigDraft<>(baseline);

    assertTrue(draft.set(option, value));

    assertTrue(draft.isDirty());
    assertEquals(value, draft.valueOf(option));
    assertEquals(value, option.valueIn(draft.edited()));
  }

  @ParameterizedTest
  @MethodSource("changes")
  <S> void changingAnOptionBackLeavesNothingToSubmit(
      S baseline, ConfigOption<S> option, String value) {
    final ConfigDraft<S> draft = new ConfigDraft<>(baseline);

    draft.set(option, value);
    draft.set(option, option.valueIn(baseline));

    assertFalse(draft.isDirty());
  }

  @ParameterizedTest
  @MethodSource("refusals")
  <S> void aValueTheOptionDoesNotTakeIsRefusedAndChangesNothing(
      S baseline, ConfigOption<S> option, String value) {
    final ConfigDraft<S> draft = new ConfigDraft<>(baseline);

    assertFalse(draft.set(option, value));

    assertFalse(draft.isDirty());
    assertEquals(option.valueIn(baseline), draft.valueOf(option));
  }

  @Test
  void theDraftStartsFromWhatIsSaved() {
    final ClientPreferences saved =
        new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, true);

    assertEquals(saved, new ConfigDraft<>(saved).edited());
  }

  @Test
  void aScreenNobodyTouchedFollowsTheServerWhenACommandMovesASetting() {
    final ConfigDraft<TotalStorageConfig> draft = new ConfigDraft<>(TotalStorageConfig.defaults());
    final TotalStorageConfig moved =
        RADIUS.withValue(TotalStorageConfig.defaults(), "48").orElseThrow();

    assertTrue(draft.adopt(moved));

    assertEquals("48", draft.valueOf(RADIUS));
    assertFalse(draft.isDirty());
  }

  @Test
  void aScreenBeingEditedIsNeverOverwrittenUnderThePlayer() {
    final ConfigDraft<TotalStorageConfig> draft = new ConfigDraft<>(TotalStorageConfig.defaults());
    draft.set(SORT_BUTTON, "false");

    assertFalse(draft.adopt(RADIUS.withValue(TotalStorageConfig.defaults(), "48").orElseThrow()));

    assertEquals("false", draft.valueOf(SORT_BUTTON));
    assertEquals(RADIUS.defaultValue(), draft.valueOf(RADIUS));
  }

  @Test
  void aServerSayingWhatTheScreenAlreadyShowsChangesNothing() {
    assertFalse(
        new ConfigDraft<>(TotalStorageConfig.defaults()).adopt(TotalStorageConfig.defaults()));
  }
}
