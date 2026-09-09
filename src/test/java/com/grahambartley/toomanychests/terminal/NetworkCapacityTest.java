package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkCapacityTest {
  @Test
  void whatIsFreeIsWhatIsNotUsed() {
    NetworkCapacity capacity = new NetworkCapacity(20, 54);

    assertEquals(34, capacity.freeSlots());
    assertEquals(20, capacity.usedSlots());
    assertEquals(54, capacity.totalSlots());
  }

  @ParameterizedTest
  @CsvSource({"0,54,0", "1,54,2", "27,54,50", "53,54,99", "54,54,100", "1,1000,1"})
  void howFullItReadsRoundsUpSoAFreeSlotNeverReadsAsFull(int used, int total, int percent) {
    assertEquals(percent, new NetworkCapacity(used, total).percentUsed());
  }

  @Test
  void aSingleFreeSlotIsNotFull() {
    NetworkCapacity capacity = new NetworkCapacity(53, 54);

    assertFalse(capacity.isFull());
    assertEquals(1, capacity.freeSlots());
  }

  @Test
  void aNetworkWithNoFreeSlotIsFull() {
    assertTrue(new NetworkCapacity(54, 54).isFull());
  }

  @Test
  void aNetworkWithNoWritableContainersIsUnknownRatherThanFull() {
    NetworkCapacity nothing = NetworkCapacity.EMPTY;

    assertTrue(nothing.isUnknown());
    assertFalse(nothing.isFull());
    assertEquals(0, nothing.percentUsed());
    assertFalse(nothing.isNearCapacity(90));
  }

  @Test
  void theWarningStartsAtTheThresholdRatherThanPastIt() {
    assertTrue(new NetworkCapacity(90, 100).isNearCapacity(90));
    assertFalse(new NetworkCapacity(89, 100).isNearCapacity(90));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void aThresholdOfNothingNeverWarns(int threshold) {
    assertFalse(new NetworkCapacity(100, 100).isNearCapacity(threshold));
  }

  @Test
  void aThresholdOfAHundredWarnsOnlyWhenReallyFull() {
    assertTrue(new NetworkCapacity(54, 54).isNearCapacity(100));
    assertFalse(new NetworkCapacity(53, 54).isNearCapacity(100));
  }

  @Test
  void usingMoreSlotsThanExistIsRefusedRatherThanDrawn() {
    assertThrows(IllegalArgumentException.class, () -> new NetworkCapacity(10, 5));
  }

  @ParameterizedTest
  @CsvSource({"-1,5", "5,-1"})
  void countingBelowZeroIsRefused(int used, int total) {
    assertThrows(IllegalArgumentException.class, () -> new NetworkCapacity(used, total));
  }

  private static ContainerRoleEntry row(ContainerRole role, int used, int total) {
    return new ContainerRoleEntry(BlockPos.ORIGIN, role, Optional.empty(), 0, 0L, used, total, 0.0);
  }

  @Test
  void theRoomOfANetworkIsTheRoomOfItsWritableContainersAddedUp() {
    NetworkCapacity capacity =
        NetworkCapacity.of(
            List.of(
                row(ContainerRole.GENERAL, 5, 27),
                row(ContainerRole.FILTERED, 9, 27),
                row(ContainerRole.GENERAL, 1, 54)));

    assertEquals(15, capacity.usedSlots());
    assertEquals(108, capacity.totalSlots());
    assertEquals(93, capacity.freeSlots());
  }

  @Test
  void anExcludedContainerIsCountedInNeitherTotal() {
    NetworkCapacity capacity =
        NetworkCapacity.of(
            List.of(row(ContainerRole.GENERAL, 5, 27), row(ContainerRole.EXCLUDED, 20, 27)));

    assertEquals(5, capacity.usedSlots());
    assertEquals(27, capacity.totalSlots());
  }

  @Test
  void aContainerThatCouldNotBeReadContributesNothing() {
    NetworkCapacity capacity =
        NetworkCapacity.of(
            List.of(row(ContainerRole.GENERAL, 0, 0), row(ContainerRole.GENERAL, 3, 27)));

    assertEquals(3, capacity.usedSlots());
    assertEquals(27, capacity.totalSlots());
  }

  @Test
  void aNetworkOfNoRowsKnowsNoRoom() {
    assertTrue(NetworkCapacity.of(List.of()).isUnknown());
  }

  @Test
  void aNetworkWhoseEveryContainerIsExcludedKnowsNoRoom() {
    NetworkCapacity capacity = NetworkCapacity.of(List.of(row(ContainerRole.EXCLUDED, 20, 27)));

    assertTrue(capacity.isUnknown());
    assertFalse(capacity.isFull());
  }
}
