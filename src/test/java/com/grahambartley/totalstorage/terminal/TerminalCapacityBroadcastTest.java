package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalCapacityBroadcastTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 20, 40, 1200, -20})
  void theSweepComesRoundOnceASecond(int ticks) {
    assertTrue(TerminalCapacityBroadcast.INTERVAL.isDue(ticks));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 19, 21, 39, 1199, -1})
  void everyTickBetweenSweepsIsLeftAlone(int ticks) {
    assertFalse(TerminalCapacityBroadcast.INTERVAL.isDue(ticks));
  }
}
