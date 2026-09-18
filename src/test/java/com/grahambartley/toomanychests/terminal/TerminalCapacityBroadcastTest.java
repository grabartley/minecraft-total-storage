package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalCapacityBroadcastTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 20, 40, 1200})
  void theSweepComesRoundOnceEveryInterval(int ticks) {
    assertTrue(TerminalCapacityBroadcast.isDue(ticks));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 19, 21, 39, 1199})
  void everyTickBetweenSweepsIsLeftAlone(int ticks) {
    assertFalse(TerminalCapacityBroadcast.isDue(ticks));
  }

  @ParameterizedTest
  @ValueSource(ints = {-20, -40})
  void aTickCounterThatHasWrappedPastZeroStillLandsOnTheInterval(int ticks) {
    assertTrue(TerminalCapacityBroadcast.isDue(ticks));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -19})
  void aTickCounterThatHasWrappedPastZeroStillSkipsTheRest(int ticks) {
    assertFalse(TerminalCapacityBroadcast.isDue(ticks));
  }
}
