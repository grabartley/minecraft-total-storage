package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FilterColumnTextTest {
  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, "tag.item.total-storage.preset." + id, true, 0, 4);
  }

  private static NetworkTagEntry custom(String name) {
    return new NetworkTagEntry(name, name, false, 2, 4);
  }

  @Test
  void aCustomTagIsMarkedInTheColumnSoItsOwnerKnowsItIsTheirs() {
    assertTrue(
        FilterColumnText.columnNameOf(custom("Castle"))
            .getString()
            .startsWith(FilterColumnText.CUSTOM_MARKER),
        "a custom tag should be marked apart from the presets");
  }

  @Test
  void aPresetIsNotMarkedBecauseItCannotBeEdited() {
    assertFalse(
        FilterColumnText.columnNameOf(preset("wood"))
            .getString()
            .startsWith(FilterColumnText.CUSTOM_MARKER));
  }

  @Test
  void theMarkerIsShapeRatherThanColourSoItSurvivesGreyscale() {
    assertFalse(FilterColumnText.CUSTOM_MARKER.isBlank());
  }

  @Test
  void aTagIsNamedWithoutItsMarkerAwayFromTheColumn() {
    assertEquals("Castle", FilterColumnText.nameOf(custom("Castle")).getString());
  }

  @ParameterizedTest
  @CsvSource({"0,0", "12,12", "10000,10.0k"})
  void aTagCountsWhatItMatchesHereTheWayACellDoes(int matching, String expected) {
    assertEquals(
        expected,
        FilterColumnText.count(new NetworkTagEntry("castle", "Castle", false, 0, matching)));
  }

  @Test
  void narrowingAndBroadeningAreNamedUnderDifferentKeys() {
    assertFalse(
        FilterColumnText.mode(TagCombinationMode.AND)
            .getString()
            .equals(FilterColumnText.mode(TagCombinationMode.OR).getString()));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void withNoFilterOnTheModeSaysOnlyWhatItCombines(int active) {
    assertEquals(
        FilterColumnText.mode(TagCombinationMode.AND).getString(),
        FilterColumnText.mode(TagCombinationMode.AND, active).getString());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 9})
  void anActiveFilterIsCountedBesideTheModeSoAScrolledListStillSaysSo(int active) {
    final Text counted = FilterColumnText.mode(TagCombinationMode.AND, active);

    assertNotEquals(FilterColumnText.mode(TagCombinationMode.AND).getString(), counted.getString());
    assertTrue(
        List.of(((TranslatableTextContent) counted.getContent()).getArgs()).contains(active));
  }

  @Test
  void aCustomTagIsListedInTheFormTheSearchBoxExpects() {
    final NetworkTagEntry tag = new NetworkTagEntry("base-kit", "Base Kit", false, 1, 1);

    assertEquals(
        FilterColumnText.CUSTOM_MARKER + "base-kit",
        FilterColumnText.columnNameOf(tag).getString());
    assertEquals("Base Kit", FilterColumnText.nameOf(tag).getString());
  }
}
