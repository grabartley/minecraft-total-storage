package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FilterColumnTextTest {
  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, "tag.item.too-many-chests.preset." + id, true, 0, 4);
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
}
