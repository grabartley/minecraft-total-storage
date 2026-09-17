package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TagTextTest {
  private static NetworkTagEntry holding(int items) {
    return new NetworkTagEntry("castle", "Castle", false, items, 0);
  }

  @Test
  void oneTaggedItemIsNotCalledItems() {
    assertFalse(
        TagText.row(holding(1)).getString().contains("items"),
        "a tag holding one item should not read as plural");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 2, 12})
  void anyOtherCountReadsAsPlural(int items) {
    assertEquals(
        TerminalScreenText.of("tags.row", "Castle", items).getString(),
        TagText.row(holding(items)).getString());
  }

  @Test
  void oneMatchingItemIsNotOfferedAsAPluralBulkAction() {
    assertEquals(
        TerminalScreenText.of("tags.assign.bulk.one").getString(), TagText.bulkHint(1).getString());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 2, 40})
  void anyOtherMatchingCountIsOfferedWithItsNumber(int matching) {
    assertEquals(
        TerminalScreenText.of("tags.assign.bulk", matching).getString(),
        TagText.bulkHint(matching).getString());
  }

  @Test
  void theTaggingHintNamesTheModifierKeyThePlatformActuallyUses() {
    assertEquals(
        TerminalScreenText.of("tags.assign.hint.mac").getString(),
        TagText.assignHint(true).getString());
    assertEquals(
        TerminalScreenText.of("tags.assign.hint").getString(),
        TagText.assignHint(false).getString());
  }
}
