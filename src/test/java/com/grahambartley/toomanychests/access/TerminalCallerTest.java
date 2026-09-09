package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TerminalCallerTest {
  private static final UUID OWNER = UUID.nameUUIDFromBytes("owner".getBytes());
  private static final UUID SOMEBODY_ELSE = UUID.nameUUIDFromBytes("else".getBytes());

  private static TerminalSettings ownedTerminal() {
    return TerminalSettings.defaults().withOwner(OWNER);
  }

  @Test
  void theOwnerMayConfigureTheirOwnTerminal() {
    assertTrue(new TerminalCaller(Optional.of(OWNER), false).mayAdminister(ownedTerminal()));
  }

  @Test
  void somebodyElseMayNotHoweverTheyGotIn() {
    assertFalse(
        new TerminalCaller(Optional.of(SOMEBODY_ELSE), false).mayAdminister(ownedTerminal()));
  }

  @Test
  void anOperatorMayConfigureATerminalThatIsNotTheirs() {
    assertTrue(new TerminalCaller(Optional.of(SOMEBODY_ELSE), true).mayAdminister(ownedTerminal()));
  }

  @Test
  void theConsoleMayConfigureAnyTerminal() {
    assertTrue(TerminalCaller.of(null, true).mayAdminister(ownedTerminal()));
  }

  @Test
  void aCallerThatIsNeitherAPlayerNorAnOperatorMayConfigureNothing() {
    assertFalse(new TerminalCaller(Optional.empty(), false).mayAdminister(ownedTerminal()));
    assertFalse(TerminalCaller.of(null).mayAdminister(ownedTerminal()));
  }

  @Test
  void aCallerWithNoPlayerAtAllIsStillJudgedOnItsLevel() {
    assertTrue(TerminalCaller.of(null, true).mayAdminister(ownedTerminal()));
    assertFalse(TerminalCaller.of(null, false).mayAdminister(ownedTerminal()));
  }

  @Test
  void aCallerMissingItsPlayerIsTreatedAsHavingNoneRatherThanThrowing() {
    assertFalse(new TerminalCaller(null, false).mayAdminister(ownedTerminal()));
    assertTrue(new TerminalCaller(null, true).mayAdminister(ownedTerminal()));
  }

  @Test
  void anUnownedTerminalIsConfigurableOnlyByAnOperator() {
    assertFalse(
        new TerminalCaller(Optional.of(OWNER), false).mayAdminister(TerminalSettings.defaults()));
    assertTrue(
        new TerminalCaller(Optional.of(OWNER), true).mayAdminister(TerminalSettings.defaults()));
  }
}
