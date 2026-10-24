package com.grahambartley.totalstorage.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalAccessRulesTest {
  private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID FRIEND = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
  private static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-0000000000c3");

  @DisplayName("the owner is let in whatever mode the terminal is set to")
  @ParameterizedTest(name = "{0} access")
  @EnumSource(TerminalAccessMode.class)
  void theOwnerIsAlwaysLetIn(TerminalAccessMode mode) {
    TerminalSettings settings = owned().withAccessMode(mode);

    TerminalAccess decision = TerminalAccessRules.decide(settings, OWNER, false);

    assertTrue(decision.allowed());
    assertFalse(decision.viaOperator());
  }

  @DisplayName("an owner-locked terminal refuses everybody but its owner")
  @ParameterizedTest(name = "{0}")
  @CsvSource({"00000000-0000-0000-0000-0000000000b2", "00000000-0000-0000-0000-0000000000c3"})
  void anOwnerLockedTerminalRefusesEverybodyElse(String player) {
    TerminalSettings settings =
        owned().withAccessMode(TerminalAccessMode.OWNER).withTrusted(FRIEND);

    assertEquals(
        TerminalAccess.DENIED,
        TerminalAccessRules.decide(settings, UUID.fromString(player), false));
  }

  @Test
  void aTrustedTerminalLetsInTheTrustListAndNobodyElse() {
    TerminalSettings settings =
        owned().withAccessMode(TerminalAccessMode.TRUSTED).withTrusted(FRIEND);

    assertEquals(TerminalAccess.TRUSTED, TerminalAccessRules.decide(settings, FRIEND, false));
    assertEquals(TerminalAccess.DENIED, TerminalAccessRules.decide(settings, STRANGER, false));
  }

  @Test
  void beingOnTheTrustListDoesNotOpenAnOwnerLockedTerminal() {
    TerminalSettings settings =
        owned().withAccessMode(TerminalAccessMode.OWNER).withTrusted(FRIEND);

    assertEquals(TerminalAccess.DENIED, TerminalAccessRules.decide(settings, FRIEND, false));
  }

  @DisplayName("an open terminal refuses nobody")
  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "00000000-0000-0000-0000-0000000000a1",
    "00000000-0000-0000-0000-0000000000b2",
    "00000000-0000-0000-0000-0000000000c3"
  })
  void anOpenTerminalLetsAnybodyIn(String player) {
    TerminalSettings settings = owned().withAccessMode(TerminalAccessMode.OPEN);

    assertTrue(TerminalAccessRules.decide(settings, UUID.fromString(player), false).allowed());
  }

  @DisplayName("an operator reaches past a lock, and it is reported as a bypass")
  @ParameterizedTest(name = "{0} access")
  @EnumSource(
      value = TerminalAccessMode.class,
      names = {"OWNER", "TRUSTED"})
  void anOperatorReachesPastALockAsABypass(TerminalAccessMode mode) {
    TerminalSettings settings = owned().withAccessMode(mode);

    assertEquals(TerminalAccess.OPERATOR, TerminalAccessRules.decide(settings, STRANGER, true));
  }

  @Test
  void anOperatorWhoOwnsTheTerminalIsNotTreatedAsABypass() {
    TerminalSettings settings = owned().withAccessMode(TerminalAccessMode.OWNER);

    assertEquals(TerminalAccess.OWNER, TerminalAccessRules.decide(settings, OWNER, true));
  }

  @Test
  void anOperatorOnTheTrustListIsNotTreatedAsABypass() {
    TerminalSettings settings =
        owned().withAccessMode(TerminalAccessMode.TRUSTED).withTrusted(FRIEND);

    assertEquals(TerminalAccess.TRUSTED, TerminalAccessRules.decide(settings, FRIEND, true));
  }

  @DisplayName("a terminal nobody owns has nobody to lock out")
  @ParameterizedTest(name = "{0} access")
  @EnumSource(TerminalAccessMode.class)
  void anUnownedTerminalLetsAnybodyIn(TerminalAccessMode mode) {
    TerminalSettings settings = TerminalSettings.defaults().withAccessMode(mode);

    assertTrue(TerminalAccessRules.decide(settings, STRANGER, false).allowed());
  }

  @Test
  void nothingIsDecidedWithoutSettingsOrAPlayer() {
    assertEquals(TerminalAccess.DENIED, TerminalAccessRules.decide(null, OWNER, true));
    assertEquals(TerminalAccess.DENIED, TerminalAccessRules.decide(owned(), null, true));
  }

  @Test
  void onlyTheOwnerAndOperatorsMayConfigureATerminal() {
    TerminalSettings settings = owned();

    assertTrue(TerminalAccessRules.mayAdminister(settings, OWNER, false));
    assertTrue(TerminalAccessRules.mayAdminister(settings, STRANGER, true));
    assertFalse(TerminalAccessRules.mayAdminister(settings, STRANGER, false));
  }

  @Test
  void anOpenTerminalIsStillOnlyConfigurableByItsOwner() {
    TerminalSettings settings = owned().withAccessMode(TerminalAccessMode.OPEN);

    assertTrue(TerminalAccessRules.decide(settings, STRANGER, false).allowed());
    assertFalse(TerminalAccessRules.mayAdminister(settings, STRANGER, false));
  }

  @Test
  void anUnownedTerminalIsConfigurableOnlyByOperators() {
    TerminalSettings settings = TerminalSettings.defaults();

    assertFalse(TerminalAccessRules.mayAdminister(settings, STRANGER, false));
    assertTrue(TerminalAccessRules.mayAdminister(settings, STRANGER, true));
  }

  @Test
  void aConsoleWithNoPlayerBehindItStillConfiguresAsAnOperator() {
    assertTrue(TerminalAccessRules.mayAdminister(owned(), null, true));
    assertFalse(TerminalAccessRules.mayAdminister(owned(), null, false));
    assertFalse(TerminalAccessRules.mayAdminister(null, null, true));
  }

  private static TerminalSettings owned() {
    return TerminalSettings.defaults().withOwner(OWNER);
  }
}
