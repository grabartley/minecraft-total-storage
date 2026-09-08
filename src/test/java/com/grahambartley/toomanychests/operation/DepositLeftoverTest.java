package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * What a leftover refuses to be.
 *
 * <p>Only the missing-item rule is reachable without a registry, since naming an amount needs an
 * {@code ItemVariant} to name it of. That an amount of nothing is refused, and that a real leftover
 * reports what stayed behind, are proven in {@code DepositServiceGameTest}.
 */
class DepositLeftoverTest {

  @Test
  void aLeftoverWithoutAnItemIsRefusedRatherThanReported() {
    assertThrows(NullPointerException.class, () -> new DepositLeftover(null, 5L));
  }
}
