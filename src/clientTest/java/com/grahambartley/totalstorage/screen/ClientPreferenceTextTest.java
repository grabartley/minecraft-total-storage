package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ClientPreference;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ClientPreferenceTextTest {
  private static final String PREFIX = "screen.total-storage.config.";

  private static TranslatableTextContent contentOf(Object text) {
    return (TranslatableTextContent) ((Text) text).getContent();
  }

  @Test
  void theSectionSaysTheseAreThePlayersOwnOptions() {
    assertEquals(PREFIX + "section.display", contentOf(ClientPreferenceText.section()).getKey());
  }

  @ParameterizedTest
  @CsvSource({
    "SORT_BUTTON_VISIBLE, sortButtonVisible, Show the sort button",
    "SCREEN_BUTTON_ANCHOR, screenButtonAnchor, Sort and stack buttons",
    "LOCKED_SLOT_TINT_VISIBLE, lockedSlotTintVisible, Tint locked slots"
  })
  void everyOptionHasAPlainLabel(ClientPreference preference, String key, String english) {
    final Text label = ClientPreferenceText.label(preference);

    assertEquals(PREFIX + "preference." + key, contentOf(label).getKey());
    assertEquals(english, label.getString());
  }

  @ParameterizedTest
  @CsvSource({
    "SORT_BUTTON_VISIBLE, true",
    "SORT_BUTTON_VISIBLE, false",
    "LOCKED_SLOT_TINT_VISIBLE, true",
    "LOCKED_SLOT_TINT_VISIBLE, false"
  })
  void aSwitchReadsAsVanillasOnOrOff(ClientPreference preference, boolean on) {
    assertEquals(
        ScreenTexts.onOrOff(on),
        ClientPreferenceText.displayValue(preference, Boolean.toString(on)));
  }

  @ParameterizedTest
  @CsvSource({
    "inside_top_right, Inside top",
    "outside_top_right, Outside top",
    "outside_bottom_right, Outside bottom"
  })
  void eachButtonPositionHasItsOwnWords(String id, String english) {
    final Text shown = ClientPreferenceText.displayValue(ClientPreference.SCREEN_BUTTON_ANCHOR, id);

    assertEquals(PREFIX + "value.anchor." + id, contentOf(shown).getKey());
    assertEquals(english, shown.getString());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void everyChoiceReadsDifferently(ClientPreference preference) {
    final Set<String> shown = new HashSet<>();
    for (final String choice : preference.choices()) {
      shown.add(ClientPreferenceText.displayValue(preference, choice).getString());
    }

    assertEquals(preference.choices().size(), shown.size());
  }

  @ParameterizedTest
  @EnumSource(ClientPreference.class)
  void theDefaultLineNamesTheDefaultInTheSameWordsAsTheButton(ClientPreference preference) {
    final TranslatableTextContent line = contentOf(ClientPreferenceText.defaultLine(preference));

    assertEquals(PREFIX + "default", line.getKey());
    assertEquals(
        ClientPreferenceText.displayValue(preference, preference.defaultValue()),
        line.getArgs()[0]);
  }

  @Test
  void anUnknownPositionShowsItsRawIdRatherThanNothing() {
    assertTrue(
        ClientPreferenceText.displayValue(ClientPreference.SCREEN_BUTTON_ANCHOR, "sideways")
            .getString()
            .contains("sideways"));
  }
}
