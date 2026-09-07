package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.lock.LockedSlots;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The bound a request is held to before anything acts on it.
 *
 * <p>Building a whole payload needs an {@code ItemVariant} and therefore a registry, so what
 * survives the wire is proven in {@code WithdrawServiceGameTest} instead.
 */
class WithdrawRequestPayloadTest {

  @ParameterizedTest
  @ValueSource(ints = {Integer.MAX_VALUE, 100_000})
  void anAmountLargerThanAnyoneCouldCarryIsClampedRatherThanRefused(int amount) {
    assertEquals(WithdrawRequestPayload.MAX_AMOUNT, WithdrawRequestPayload.boundedAmount(amount));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  void anAmountBelowNothingBecomesNothing(int amount) {
    assertEquals(0, WithdrawRequestPayload.boundedAmount(amount));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 64, 2000})
  void anAmountWithinBoundsIsCarriedUnchanged(int amount) {
    assertEquals(amount, WithdrawRequestPayload.boundedAmount(amount));
  }

  @Test
  void theCeilingIsWhatThePlayersOwnSlotsCouldEverHold() {
    assertEquals(
        LockedSlots.SLOT_COUNT * WithdrawRequestPayload.MAX_STACK_SIZE,
        WithdrawRequestPayload.MAX_AMOUNT);
  }
}
