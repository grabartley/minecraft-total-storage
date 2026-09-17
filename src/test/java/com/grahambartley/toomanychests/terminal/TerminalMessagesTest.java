package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(
        false,
        TerminalMessages.highlighted(2)
            .getString()
            .equals(TerminalMessages.rescanned(2).getString()));
  }
}
