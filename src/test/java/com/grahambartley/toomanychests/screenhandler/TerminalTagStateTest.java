package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class TerminalTagStateTest {
  @Test
  void aFreshStateIsClosedAndWatchingNothing() {
    final TerminalTagState state = new TerminalTagState();
    assertFalse(state.isOpen());
    assertEquals(Optional.empty(), state.subject());
  }

  @Test
  void openingWithoutASubjectWatchesTheWholeNetwork() {
    final TerminalTagState state = new TerminalTagState();
    state.requested(true, Optional.empty());
    assertTrue(state.isOpen());
    assertEquals(Optional.empty(), state.subject());
  }

  @Test
  void closingForgetsTheSubjectItWasWatching() {
    final TerminalTagState state = new TerminalTagState();
    state.requested(true, Optional.empty());
    state.closed();
    assertFalse(state.isOpen());
    assertEquals(Optional.empty(), state.subject());
  }

  @Test
  void aClosedRequestNeverKeepsASubject() {
    final TerminalTagState state = new TerminalTagState();
    state.requested(false, null);
    assertFalse(state.isOpen());
    assertEquals(Optional.empty(), state.subject());
  }
}
