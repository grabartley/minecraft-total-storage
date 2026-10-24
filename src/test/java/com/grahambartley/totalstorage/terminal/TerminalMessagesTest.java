package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalMessagesTest {
  @ParameterizedTest
  @CsvSource({"0,0 containers", "1,1 container", "4,4 containers"})
  void aHighlightSaysHowManyContainersItOutlined(int containers, String expected) {
    assertEquals(
        "Outlining " + expected + " in the world",
        TerminalMessages.highlighted(containers).getString());
  }

  @Test
  void aRescanAndAHighlightReadAsDifferentThings() {
    assertNotEquals(
        TerminalMessages.rescanned(2).getString(), TerminalMessages.highlighted(2).getString());
  }
}
