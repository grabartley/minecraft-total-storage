package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalPermissionTest {

  @ParameterizedTest
  @EnumSource(TerminalPermission.class)
  void everyPermissionSurvivesBeingNamedAndReadBack(TerminalPermission permission) {
    assertEquals(Optional.of(permission), TerminalPermission.fromId(permission.id()));
  }

  @ParameterizedTest
  @EnumSource(TerminalPermission.class)
  void anIdIsLowerCaseSoItReadsTheSameOnTheWireAsInACommand(TerminalPermission permission) {
    assertEquals(permission.name().toLowerCase(java.util.Locale.ROOT), permission.id());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMINISTER", "  administer ", "Administer"})
  void anIdIsReadBackWhateverItsCaseOrSurroundingSpace(String id) {
    assertEquals(Optional.of(TerminalPermission.ADMINISTER), TerminalPermission.fromId(id));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "owner", "root", "administrator"})
  void anIdThisBuildDoesNotKnowIsNoPermissionAtAll(String id) {
    assertEquals(Optional.empty(), TerminalPermission.fromId(id));
  }

  @Test
  void onlyAdministerMayAdminister() {
    assertTrue(TerminalPermission.ADMINISTER.mayAdminister());
    assertFalse(TerminalPermission.USE.mayAdminister());
  }

  @Test
  void aPlayerWhoMayAdministerIsGivenTheAdministerPermission() {
    assertEquals(TerminalPermission.ADMINISTER, TerminalPermission.of(true));
    assertEquals(TerminalPermission.USE, TerminalPermission.of(false));
  }
}
