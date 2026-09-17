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
  @CsvSource({"64, NO_STOCK", "63, NO_STOCK", "1, NO_STOCK", "0, NO_ROOM"})
  void theRoomLeftAtTheDestinationDecidesWhichSideRanOut(long room, ShortfallCause expected) {
    assertEquals(expected, causeOf(new FixedRoom(room)));
  }

  @Test
  void aShortfallCannotBeDecidedWithoutTheThingsItIsDecidedFrom() {
    try (Transaction outer = Transaction.openOuter()) {
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(null, DIAMOND, A_STACK, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(new FixedRoom(1L), null, A_STACK, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(new FixedRoom(1L), DIAMOND, A_STACK, null));
    }
  }

  private static ShortfallCause causeOf(Storage<String> destination) {
    try (Transaction outer = Transaction.openOuter()) {
      return WithdrawShortfall.causeOf(destination, DIAMOND, A_STACK, outer);
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
