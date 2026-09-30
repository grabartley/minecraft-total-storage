package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TrustedPlayerNamesTest {
  @Test
  void withNoServerToAskAPlayerIsShownByTheirUuid() {
    UUID player = UUID.randomUUID();

    assertEquals(player.toString(), TrustedPlayerNames.nameOf(null, player));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void aBlankNameFindsNobody(String name) {
    assertTrue(TrustedPlayerNames.find(null, name).isEmpty());
  }

  @Test
  void withNoServerToAskNobodyIsFound() {
    assertTrue(TrustedPlayerNames.find(null, "Steve").isEmpty());
  }
}
