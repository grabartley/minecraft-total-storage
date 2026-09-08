package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Who a terminal will let configure it, asked the same way whatever the question arrived through.
 *
 * <p>The point of the type is that a screen and a console reach one rule rather than two, so what
 * is proven here is that both shapes of caller land on ADR 0008's answer.
 */
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

  /**
   * ADR 0008 accepts the operator bypass, and logging it is the mitigation rather than refusing.
   */
  @Test
  void anOperatorMayConfigureATerminalThatIsNotTheirs() {
    assertTrue(new TerminalCaller(Optional.of(SOMEBODY_ELSE), true).mayAdminister(ownedTerminal()));
  }

  /** A console has no ownership to appeal to, so it stands on its operator level alone. */
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

  /** A terminal nobody owns cannot be locked, so configuring one is operators only. */
  @Test
  void anUnownedTerminalIsConfigurableOnlyByAnOperator() {
    assertFalse(
        new TerminalCaller(Optional.of(OWNER), false).mayAdminister(TerminalSettings.defaults()));
    assertTrue(
        new TerminalCaller(Optional.of(OWNER), true).mayAdminister(TerminalSettings.defaults()));
  }
}
