package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalListingBroadcastTest {
  @ParameterizedTest(name = "starting at tick {0}")
  @ValueSource(ints = {0, 1, 3, 997})
  void theSweepRunsFiveTimesASecondWhateverTickItStartsOn(int start) {
    long sweeps =
        IntStream.range(start, start + 20).filter(TerminalListingBroadcast.INTERVAL::isDue).count();

    assertEquals(5L, sweeps);
  }
}
