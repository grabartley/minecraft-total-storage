package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalAccessModeTest {
  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void everyModeRoundTripsThroughItsId(TerminalAccessMode mode) {
    assertEquals(Optional.of(mode), TerminalAccessMode.fromId(mode.id()));
  }

  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void idsAreLowercase(TerminalAccessMode mode) {
    assertEquals(mode.id().toLowerCase(java.util.Locale.ROOT), mode.id());
  }

  @ParameterizedTest
  @ValueSource(strings = {"OWNER", " owner ", "Owner"})
  void parsingIgnoresCaseAndSurroundingSpace(String id) {
    assertEquals(Optional.of(TerminalAccessMode.OWNER), TerminalAccessMode.fromId(id));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "admin", "owner-only"})
  void anUnrecognisedIdIsEmpty(String id) {
    assertTrue(TerminalAccessMode.fromId(id).isEmpty());
  }

  @Test
  void aNullIdIsEmpty() {
    assertTrue(TerminalAccessMode.fromId(null).isEmpty());
  }
}
