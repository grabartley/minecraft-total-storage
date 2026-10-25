package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FilterColumnTextTest {
  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, "tag.item.total-storage.preset." + id, true, 0, 4);
  }

  private static NetworkTagEntry custom(String name) {
    return new NetworkTagEntry(name, name, false, 2, 4);
  }

  @Test
  void aCustomTagIsNamedAsItsOwnerTypedIt() {
    assertEquals("Castle", FilterColumnText.nameOf(custom("Castle")).getString());
  }

  @Test
  void aPresetIsNamedThroughItsTranslation() {
    assertEquals(
        "tag.item.total-storage.preset.wood",
        ((TranslatableTextContent) FilterColumnText.nameOf(preset("wood")).getContent()).getKey());
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
