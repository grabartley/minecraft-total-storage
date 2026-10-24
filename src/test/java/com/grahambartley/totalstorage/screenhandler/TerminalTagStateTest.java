package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @Test
  void nothingHasBeenSentToAFreshState() {
    final TerminalTagState state = new TerminalTagState();
    assertEquals(Optional.empty(), state.lastSent());
    assertTrue(state.differsFromLastSent(view(0)));
  }

  @ParameterizedTest
  @MethodSource("comparisons")
  void aViewDiffersOnlyWhenItsContentDoes(int sentCount, int nextCount, boolean differs) {
    final TerminalTagState state = new TerminalTagState();
    state.sent(view(sentCount));
    assertEquals(differs, state.differsFromLastSent(view(nextCount)));
  }

  private static Stream<Arguments> comparisons() {
    return Stream.of(Arguments.of(0, 0, false), Arguments.of(0, 1, true), Arguments.of(3, 2, true));
  }

  @ParameterizedTest
  @MethodSource("resets")
  void reopeningOrClosingForgetsWhatWasSent(Consumer<TerminalTagState> reset) {
    final TerminalTagState state = new TerminalTagState();
    state.requested(true, Optional.empty());
    state.sent(view(4));
    reset.accept(state);
    assertEquals(Optional.empty(), state.lastSent());
    assertTrue(state.differsFromLastSent(view(4)));
  }

  private static Stream<Arguments> resets() {
    return Stream.of(
        Arguments.of((Consumer<TerminalTagState>) TerminalTagState::closed),
        Arguments.of(
            (Consumer<TerminalTagState>) state -> state.requested(true, Optional.empty())));
  }

  private static NetworkTagView view(int matching) {
    return new NetworkTagView(
        true,
        List.of(new NetworkTagEntry("castle", "Castle", false, 1, matching)),
        Optional.empty(),
        Set.of());
  }
}
