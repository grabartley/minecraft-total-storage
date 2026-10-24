package com.grahambartley.totalstorage.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
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
  void theOwnerMayOpenTheirOwnTerminal() {
    assertTrue(new TerminalCaller(Optional.of(OWNER), false).mayOpen(ownedTerminal()));
  }

  @Test
  void somebodyElseMayNotOpenAnOwnerLockedTerminal() {
    assertFalse(new TerminalCaller(Optional.of(SOMEBODY_ELSE), false).mayOpen(ownedTerminal()));
  }

  @Test
  void aTrustedPlayerMayOpenButStillMayNotConfigure() {
    final TerminalSettings trusting =
        ownedTerminal().withAccessMode(TerminalAccessMode.TRUSTED).withTrusted(SOMEBODY_ELSE);
    final TerminalCaller trusted = new TerminalCaller(Optional.of(SOMEBODY_ELSE), false);

    assertTrue(trusted.mayOpen(trusting));
    assertFalse(trusted.mayAdminister(trusting));
  }

  @Test
  void anOperatorMayOpenATerminalThatIsNotTheirs() {
    assertTrue(new TerminalCaller(Optional.of(SOMEBODY_ELSE), true).mayOpen(ownedTerminal()));
  }

  @Test
  void theConsoleMayOpenNothingBecauseThereIsNobodyToShowItTo() {
    assertFalse(TerminalCaller.of(null, true).mayOpen(ownedTerminal()));
  }

  @Test
  void theConsoleStillReadsAnyNetworkEvenThoughItCannotOpenOne() {
    assertTrue(TerminalCaller.of(null, true).maySeeMembership(ownedTerminal()));
  }

  @Test
  void membershipIsReadableByWhoeverMayOpenTheTerminal() {
    assertTrue(new TerminalCaller(Optional.of(OWNER), false).maySeeMembership(ownedTerminal()));
    assertTrue(
        new TerminalCaller(Optional.of(SOMEBODY_ELSE), true).maySeeMembership(ownedTerminal()));
  }

  @Test
  void aStrangerReadsNothingAboutANetworkTheyMayNotOpen() {
    assertFalse(
        new TerminalCaller(Optional.of(SOMEBODY_ELSE), false).maySeeMembership(ownedTerminal()));
    assertFalse(TerminalCaller.of(null, false).maySeeMembership(ownedTerminal()));
  }

  @Test
  void anUnownedTerminalIsConfigurableOnlyByAnOperator() {
    assertFalse(
        new TerminalCaller(Optional.of(OWNER), false).mayAdminister(TerminalSettings.defaults()));
    assertTrue(
        new TerminalCaller(Optional.of(OWNER), true).mayAdminister(TerminalSettings.defaults()));
  }
}
