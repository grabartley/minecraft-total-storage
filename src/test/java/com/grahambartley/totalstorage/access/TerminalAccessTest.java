package com.grahambartley.totalstorage.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalAccessTest {
  @DisplayName("every reason but refusal lets the player through")
  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = TerminalAccess.class,
      names = {"DENIED"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyReasonButRefusalAllows(TerminalAccess access) {
    assertTrue(access.allowed());
  }

  @Test
  void onlyRefusalRefuses() {
    assertFalse(TerminalAccess.DENIED.allowed());
  }

  @Test
  void onlyTheOperatorBypassCountsAsABypass() {
    assertEquals(
        1, Arrays.stream(TerminalAccess.values()).filter(TerminalAccess::viaOperator).count());
    assertTrue(TerminalAccess.OPERATOR.viaOperator());
  }
}
