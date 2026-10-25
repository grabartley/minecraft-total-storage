package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalNoticeToneTest {
  @ParameterizedTest
  @CsvSource({"DONE, false", "PARTIAL, true", "FAILED, true"})
  void onlyAResultThatFellShortIsShownInTheTerminal(TerminalNoticeTone tone, boolean shown) {
    assertEquals(shown, tone.isShownInTheTerminal());
  }
}
