package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class AccessModeCycleTest {

  /**
   * Clicking opens the terminal up a step at a time, so the cautious mode is the one reached first.
   */
  @ParameterizedTest
  @CsvSource({"OWNER, TRUSTED", "TRUSTED, OPEN", "OPEN, OWNER"})
  void clickingLetsMorePeopleIn(TerminalAccessMode from, TerminalAccessMode to) {
    assertEquals(to, AccessModeCycle.next(from));
  }

  @ParameterizedTest
  @CsvSource({"TRUSTED, OWNER", "OPEN, TRUSTED", "OWNER, OPEN"})
  void goingBackShutsTheDoorAgain(TerminalAccessMode from, TerminalAccessMode to) {
    assertEquals(to, AccessModeCycle.previous(from));
  }

  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void goingForwardsThenBackReturnsToWhereItStarted(TerminalAccessMode mode) {
    assertEquals(mode, AccessModeCycle.previous(AccessModeCycle.next(mode)));
  }

  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void threeClicksReturnToWhereItStarted(TerminalAccessMode mode) {
    assertEquals(mode, AccessModeCycle.next(AccessModeCycle.next(AccessModeCycle.next(mode))));
  }

  @Test
  void aTerminalWithNoModeIsCycledFromTheMostClosedOne() {
    assertEquals(TerminalAccessMode.TRUSTED, AccessModeCycle.next(null));
    assertEquals(TerminalAccessMode.OPEN, AccessModeCycle.previous(null));
  }
}
