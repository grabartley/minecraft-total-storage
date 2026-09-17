package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawShortfallTest {
  private static final String DIAMOND = "diamond";
  private static final long OUTSTANDING = 55L;

  @ParameterizedTest
  @CsvSource({"64, NO_ROOM", "1, NO_ROOM", "0, NO_STOCK"})
  void whatTheNetworkStillHoldsDecidesWhichSideRanOut(long stock, ShortfallCause expected) {
    assertEquals(expected, causeOf(List.of(new FixedStock(stock))));
  }

  @Test
  void aNetworkThatIsEmptyEverywhereBlamesTheStockRatherThanTheRoom() {
    assertEquals(ShortfallCause.NO_STOCK, causeOf(List.of(new FixedStock(0L), new FixedStock(0L))));
  }

  @Test
  void stockLeftInAnySourceAtAllMeansTheRoomIsWhatStoppedIt() {
    assertEquals(ShortfallCause.NO_ROOM, causeOf(List.of(new FixedStock(0L), new FixedStock(3L))));
  }

  @Test
  void aNetworkWithNoSourcesLeftHasNothingToBlameButItsStock() {
    assertEquals(ShortfallCause.NO_STOCK, causeOf(List.of()));
  }

  @Test
  void aShortfallCannotBeDecidedWithoutTheThingsItIsDecidedFrom() {
    try (Transaction outer = Transaction.openOuter()) {
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(null, DIAMOND, OUTSTANDING, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(List.of(new FixedStock(1L)), null, OUTSTANDING, outer));
      assertThrows(
          NullPointerException.class,
          () -> WithdrawShortfall.causeOf(List.of(new FixedStock(1L)), DIAMOND, OUTSTANDING, null));
    }
  }

  private static ShortfallCause causeOf(List<Storage<String>> sources) {
    try (Transaction outer = Transaction.openOuter()) {
      return WithdrawShortfall.causeOf(sources, DIAMOND, OUTSTANDING, outer);
    }
  }

  private record FixedStock(long stock) implements Storage<String> {
    @Override
    public long insert(String resource, long maxAmount, TransactionContext transaction) {
      return 0L;
    }

    @Override
    public long extract(String resource, long maxAmount, TransactionContext transaction) {
      return Math.min(stock, maxAmount);
    }

    @Override
    public Iterator<StorageView<String>> iterator() {
      return Collections.emptyIterator();
    }
  }
}
