package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DepositLeftoverTest {
  @Test
  void aLeftoverWithoutAnItemIsRefusedRatherThanReported() {
    assertThrows(NullPointerException.class, () -> new DepositLeftover(null, 5L));
  }
}
