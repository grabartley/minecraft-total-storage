package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpensiveOperationTest {
  @ParameterizedTest
  @CsvSource({"RESCAN,rescan", "DEPOSIT_ALL,deposit_all"})
  void everyOperationIsNamedUnderTheKeyTheLangFileCarries(ExpensiveOperation operation, String id) {
    assertEquals(id, operation.id());
  }
}
