package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalOpenStateTest {
  @Test
  void onlyAReadyTerminalOpens() {
    assertTrue(TerminalOpenState.READY.allowed());
  }

  @ParameterizedTest
  @EnumSource(
      value = TerminalOpenState.class,
      names = {"READY"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOtherStateIsARefusal(TerminalOpenState state) {
    assertFalse(state.allowed());
  }

  @ParameterizedTest
  @EnumSource(
      value = TerminalOpenState.class,
      names = {"UNPOWERED", "DENIED"})
  void aRefusalTheyCanActOnIsExplained(TerminalOpenState state) {
    assertTrue(state.refusal().isPresent());
  }

  @ParameterizedTest
  @EnumSource(
      value = TerminalOpenState.class,
      names = {"READY", "MISSING", "REFUSED"})
  void aRefusalThereIsNothingToSayAboutStaysQuiet(TerminalOpenState state) {
    assertEquals(java.util.Optional.empty(), state.refusal());
  }

  @Test
  void thePoweredAndTheForbiddenAreToldDifferentThings() {
    assertFalse(
        TerminalOpenState.UNPOWERED
            .refusal()
            .orElseThrow()
            .equals(TerminalOpenState.DENIED.refusal().orElseThrow()));
  }
}
