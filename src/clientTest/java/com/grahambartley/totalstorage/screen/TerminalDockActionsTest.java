package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.terminal.TerminalAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalDockActionsTest {
  private final TerminalPrompts prompts = new TerminalPrompts(3);

  @ParameterizedTest
  @CsvSource({"SORT_NETWORK, SORT_NETWORK", "CONSOLIDATE, CONSOLIDATE"})
  void eachNetworkButtonAsksForItsOwnOperation(
      ExpensiveOperation operation, TerminalAction expected) {
    assertEquals(expected, TerminalDockActions.actionFor(operation));
  }

  @Test
  void newTagOpensTheNamingPrompt() {
    new TerminalDockActions(3, prompts).newTag();

    assertTrue(prompts.naming().isOpen());
  }
}
