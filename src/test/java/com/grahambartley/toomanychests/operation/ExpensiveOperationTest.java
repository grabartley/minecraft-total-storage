package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The name each rate-limited operation is spoken of under.
 *
 * <p>Written out rather than derived, because these become translation keys and a lang file cannot
 * follow a renamed enum constant. A test that recomputed the derivation would agree with a rename
 * that had quietly changed what a player reads.
 */
class ExpensiveOperationTest {

  @ParameterizedTest
  @CsvSource({"RESCAN,rescan", "DEPOSIT_ALL,deposit_all"})
  void everyOperationIsNamedUnderTheKeyTheLangFileCarries(ExpensiveOperation operation, String id) {
    assertEquals(id, operation.id());
  }
}
