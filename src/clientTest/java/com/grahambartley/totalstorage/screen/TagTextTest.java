package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TagTextTest {
  private static NetworkTagEntry holding(int items) {
    return new NetworkTagEntry("castle", "Castle", false, items, 0);
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
}
