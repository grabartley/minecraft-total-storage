package com.grahambartley.totalstorage.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TickIntervalTest {
  static Stream<Arguments> ticks() {
    return Stream.of(
        Arguments.of(1, 0, true),
        Arguments.of(1, 17, true),
        Arguments.of(4, 0, true),
        Arguments.of(4, 4, true),
        Arguments.of(4, 1200, true),
        Arguments.of(4, 3, false),
        Arguments.of(4, 5, false),
        Arguments.of(20, 40, true),
        Arguments.of(20, 39, false),
        Arguments.of(20, -20, true),
        Arguments.of(20, -1, false));
  }

  @ParameterizedTest(name = "every {0}, tick {1} due {2}")
  @MethodSource("ticks")
  void anIntervalComesRoundOnceEveryPeriodEvenPastAWrappedCounter(
      int every, int tick, boolean due) {
    assertEquals(due, new TickInterval(every).isDue(tick));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -20})
  void anIntervalShorterThanATickIsRefused(int every) {
    assertThrows(IllegalArgumentException.class, () -> new TickInterval(every));
  }
}
