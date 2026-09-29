package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.EnglishLang;
import java.util.Optional;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void everyModeIsShownWithTheTerminalsOwnWordsForIt(TerminalAccessMode mode) {
    assertEquals(
        "screen.too-many-chests.terminal.access." + mode.id(),
        ((TranslatableTextContent) mode.displayName().getContent()).getKey());
  }

  @ParameterizedTest
  @CsvSource({"OWNER, Owner only", "TRUSTED, Trusted players", "OPEN, Anyone"})
  void theModesReadAsAPlayerWouldSayThem(TerminalAccessMode mode, String english) {
    assertEquals(english, EnglishLang.of(keyOf(mode)));
  }

  private static String keyOf(TerminalAccessMode mode) {
    return ((TranslatableTextContent) mode.displayName().getContent()).getKey();
  }
}
