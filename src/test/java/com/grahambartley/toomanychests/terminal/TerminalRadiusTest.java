package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalRadiusTest {

  @DisplayName("a radius the server permits is left exactly as it was asked for")
  @ParameterizedTest(name = "{0} blocks under a maximum of {1}")
  @CsvSource({"1, 16", "8, 16", "16, 16", "64, 64"})
  void allowsARadiusWithinTheServerMaximum(int requested, int serverMaximum) {
    TerminalRadius radius = TerminalRadius.of(requested, serverMaximum);

    assertEquals(requested, radius.blocks());
    assertFalse(radius.clamped());
  }

  @DisplayName("a radius above the server maximum is clamped down to it")
  @ParameterizedTest(name = "{0} blocks under a maximum of {1} reaches {2}")
  @CsvSource({"17, 16, 16", "64, 16, 16", "5, 4, 4", "2, 1, 1"})
  void clampsARadiusAboveTheServerMaximum(int requested, int serverMaximum, int expected) {
    TerminalRadius radius = TerminalRadius.of(requested, serverMaximum);

    assertEquals(expected, radius.blocks());
    assertEquals(requested, radius.requested());
    assertTrue(radius.clamped());
  }

  @DisplayName("a radius below one is raised to the smallest a terminal may have")
  @ParameterizedTest(name = "{0} blocks")
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  void raisesARadiusBelowTheMinimum(int requested) {
    TerminalRadius radius = TerminalRadius.of(requested, 16);

    assertEquals(TerminalSettings.MIN_RADIUS, radius.blocks());
    assertTrue(radius.clamped());
  }

  @DisplayName("no radius escapes the absolute bounds, whatever a server maximum claims to be")
  @ParameterizedTest(name = "{0} blocks under a maximum of {1} reaches {2}")
  @CsvSource({"9999, 9999, 64", "64, 9999, 64", "8, 0, 1", "8, -5, 1", "9999, 16, 16", "-3, 0, 1"})
  void keepsEveryRadiusInsideTheAbsoluteBounds(int requested, int serverMaximum, int expected) {
    assertEquals(expected, TerminalRadius.of(requested, serverMaximum).blocks());
  }

  @DisplayName("the effective radius is what a scan starting now would walk")
  @ParameterizedTest(name = "configured {0} under a maximum of {1} scans {2}")
  @CsvSource({"8, 16, 8", "24, 16, 16", "24, 32, 24", "1, 16, 1"})
  void reportsTheRadiusAScanWouldWalk(int configured, int serverMaximum, int expected) {
    assertEquals(expected, TerminalRadius.effective(configured, serverMaximum));
  }

  @Test
  @DisplayName("raising the maximum again gives a held down terminal its reach back")
  void returnsTheConfiguredReachWhenTheMaximumGoesBackUp() {
    assertEquals(4, TerminalRadius.effective(24, 4));
    assertEquals(24, TerminalRadius.effective(24, 32));
  }
}
