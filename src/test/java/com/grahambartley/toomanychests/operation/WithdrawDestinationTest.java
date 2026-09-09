package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawDestinationTest {
  @ParameterizedTest
  @CsvSource({"CURSOR, true", "INVENTORY, false"})
  void onlyTheCursorIsLimitedToOneStack(WithdrawDestination destination, boolean oneStack) {
    assertEquals(oneStack, destination.holdsOneStackOnly());
  }
}
