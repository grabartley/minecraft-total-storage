package com.grahambartley.toomanychests.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OperationResultTest {
  @ParameterizedTest
  @ValueSource(longs = {0L, 1L, 64L})
  void aCommittedResultCarriesWhatTheOperationProduced(long moved) {
    OperationResult<Long> result = OperationResult.committed(moved);

    assertTrue(result.committed());
    assertFalse(result.rolledBack());
    assertEquals(Optional.of(moved), result.value());
    assertEquals(moved, result.orElse(-1L));
  }

  @Test
  void aRolledBackResultCarriesAReasonAndNoValue() {
    OperationResult<Long> result = OperationResult.rolledBack("destination full");

    assertFalse(result.committed());
    assertTrue(result.rolledBack());
    assertEquals(Optional.empty(), result.value());
    assertEquals("destination full", result.reason());
    assertEquals(-1L, result.orElse(-1L));
  }

  @Test
  void aCommittedResultWithoutAValueIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OperationResult<Long>(true, Optional.empty(), ""));
    assertThrows(NullPointerException.class, () -> OperationResult.committed(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " "})
  void aRolledBackResultWithoutAReasonIsRejected(String reason) {
    assertThrows(IllegalArgumentException.class, () -> OperationResult.<Long>rolledBack(reason));
  }
}
