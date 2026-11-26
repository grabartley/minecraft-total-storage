package com.grahambartley.totalstorage.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RescanGateTest {
  private final RescanGate gate = new RescanGate();

  @ParameterizedTest(name = "tick {0}")
  @ValueSource(ints = {0, 1, 2400, Integer.MAX_VALUE})
  void theFirstClaimInATickIsGranted(int tick) {
    assertTrue(gate.claim(tick));
  }

  @ParameterizedTest(name = "{0} more claims")
  @ValueSource(ints = {1, 2, 25})
  void laterClaimsInTheSameTickAreRefused(int more) {
    gate.claim(7);

    for (int claim = 0; claim < more; claim++) {
      assertFalse(gate.claim(7));
    }
  }

  static Stream<Arguments> tickSequences() {
    return Stream.of(
        Arguments.of(List.of(1, 1, 2, 2, 3), List.of(true, false, true, false, true)),
        Arguments.of(List.of(5, 6, 5), List.of(true, true, true)),
        Arguments.of(List.of(0, 0, 0), List.of(true, false, false)));
  }

  @ParameterizedTest(name = "{0} grants {1}")
  @MethodSource("tickSequences")
  void eachNewTickGrantsOneClaim(List<Integer> ticks, List<Boolean> granted) {
    for (int index = 0; index < ticks.size(); index++) {
      assertEquals(granted.get(index), gate.claim(ticks.get(index)), "claim " + index);
    }
  }

  @ParameterizedTest(name = "tick {0}")
  @ValueSource(ints = {0, 42})
  void aResetGateGrantsTheSameTickAgain(int tick) {
    gate.claim(tick);

    gate.reset();

    assertTrue(gate.claim(tick));
  }
}
