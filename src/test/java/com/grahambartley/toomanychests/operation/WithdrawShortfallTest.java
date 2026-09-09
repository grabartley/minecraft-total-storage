package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawShortfallTest {
  private static final String DIAMOND = "diamond";
  private static final long A_STACK = 64L;

  @ParameterizedTest
  @CsvSource({
    "64, NOTHING_AVAILABLE",
    "63, NOTHING_AVAILABLE",
    "1, NOTHING_AVAILABLE",
    "0, NO_ROOM"
  })
  void theRoomLeftAtTheDestinationDecidesWhichEndingItWas(long room, WithdrawResult expected) {
    assertEquals(expected, resultOf(new FixedRoom(room)));
  }

  @Test
  void everyEndingKeepsTheAmountThatWasAskedFor() {
    assertEquals(A_STACK, outcomeOf(new FixedRoom(A_STACK)).requested());
    assertEquals(A_STACK, outcomeOf(new FixedRoom(0L)).requested());
  }

  @Test
  void nothingIsEverReportedAsMovedByAnEmptyHandedWithdrawal() {
    assertEquals(0L, outcomeOf(new FixedRoom(A_STACK)).moved());
    assertEquals(0L, outcomeOf(new FixedRoom(0L)).moved());
  }

  @Test
  void aShortfallCannotBeDecidedWithoutTheThingsItIsDecidedFrom() {
    try (Transaction outer = Transaction.openOuter()) {
      assertThrows(
          NullPointerException.class, () -> WithdrawShortfall.of(null, DIAMOND, A_STACK, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.of(new FixedRoom(1L), null, A_STACK, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.of(new FixedRoom(1L), DIAMOND, A_STACK, null));
    }
  }

  private static WithdrawResult resultOf(Storage<String> destination) {
    return outcomeOf(destination).result();
  }

  private static WithdrawOutcome outcomeOf(Storage<String> destination) {
    try (Transaction outer = Transaction.openOuter()) {
      return WithdrawShortfall.of(destination, DIAMOND, A_STACK, outer);
    }
  }

  private record FixedRoom(long room) implements Storage<String> {
    @Override
    public long insert(String resource, long maxAmount, TransactionContext transaction) {
      return Math.min(room, maxAmount);
    }

    @Override
    public long extract(String resource, long maxAmount, TransactionContext transaction) {
      return 0L;
    }

    @Override
    public Iterator<StorageView<String>> iterator() {
      return Collections.emptyIterator();
    }
  }
}
