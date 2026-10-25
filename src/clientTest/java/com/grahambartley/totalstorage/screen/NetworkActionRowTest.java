package com.grahambartley.totalstorage.screen;

import static com.grahambartley.totalstorage.operation.NetworkOperationPhase.ARMED;
import static com.grahambartley.totalstorage.operation.NetworkOperationPhase.IDLE;
import static com.grahambartley.totalstorage.operation.NetworkOperationPhase.RUNNING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class NetworkActionRowTest {
  private static final int PERCENT = 40;

  static Stream<Arguments> phases() {
    return Stream.of(
        Arguments.of(null, null, NetworkActionRow.Mode.SPLIT, null, 0),
        Arguments.of(IDLE, IDLE, NetworkActionRow.Mode.SPLIT, null, 0),
        Arguments.of(
            RUNNING, IDLE, NetworkActionRow.Mode.RUNNING, ExpensiveOperation.SORT_NETWORK, PERCENT),
        Arguments.of(
            IDLE, RUNNING, NetworkActionRow.Mode.RUNNING, ExpensiveOperation.CONSOLIDATE, PERCENT),
        Arguments.of(
            RUNNING,
            RUNNING,
            NetworkActionRow.Mode.RUNNING,
            ExpensiveOperation.SORT_NETWORK,
            PERCENT),
        Arguments.of(
            ARMED, RUNNING, NetworkActionRow.Mode.RUNNING, ExpensiveOperation.CONSOLIDATE, PERCENT),
        Arguments.of(ARMED, IDLE, NetworkActionRow.Mode.ARMED, ExpensiveOperation.SORT_NETWORK, 0),
        Arguments.of(IDLE, ARMED, NetworkActionRow.Mode.ARMED, ExpensiveOperation.CONSOLIDATE, 0),
        Arguments.of(
            ARMED, ARMED, NetworkActionRow.Mode.ARMED, ExpensiveOperation.SORT_NETWORK, 0));
  }

  @ParameterizedTest(name = "sort {0}, gather {1}")
  @MethodSource("phases")
  void aRunningOperationWinsOverAnArmedOneAndSortWinsATie(
      NetworkOperationPhase sort,
      NetworkOperationPhase gather,
      NetworkActionRow.Mode mode,
      ExpensiveOperation operation,
      int percentDone) {
    final NetworkActionRow row = NetworkActionRow.of(sort, gather, PERCENT);

    assertEquals(new NetworkActionRow(mode, operation, percentDone), row);
  }

  @Test
  void aRowWithNothingGoingOnIsSplit() {
    assertTrue(NetworkActionRow.of(IDLE, IDLE, PERCENT).isSplit());
  }

  @ParameterizedTest
  @EnumSource(value = NetworkOperationPhase.class, names = "IDLE", mode = EnumSource.Mode.EXCLUDE)
  void aRowWithAnOperationUnderwayIsNotSplit(NetworkOperationPhase phase) {
    assertFalse(NetworkActionRow.of(phase, IDLE, PERCENT).isSplit());
  }
}
