package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkTagTallyTest {
  private static final NetworkTagTally TALLY =
      NetworkTagTally.of(
          List.of(
              new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood", "building")),
              new FilterCandidate("minecraft:birch_log", "Birch Log", Set.of("wood")),
              new FilterCandidate("minecraft:stone", "Stone", Set.of("building"))));

  @ParameterizedTest
  @CsvSource({"wood,2", "building,2"})
  void aTagIsCountedOnceForEveryItemCarryingIt(String tag, int expected) {
    assertEquals(expected, TALLY.countOf(tag));
  }

  @ParameterizedTest
  @ValueSource(strings = {"redstone", "castle", ""})
  void aTagNoItemCarriesCountsZeroRatherThanGoingMissing(String tag) {
    assertEquals(0, TALLY.countOf(tag));
  }

  @Test
  void aTagIsFoundWhateverTheCaseItIsAskedFor() {
    assertEquals(2, TALLY.countOf("WOOD"));
    assertEquals(2, TALLY.countOf("  Wood  "));
  }

  @Test
  void anEmptyNetworkCountsNothing() {
    assertEquals(0, NetworkTagTally.of(List.of()).countOf("wood"));
    assertEquals(0, NetworkTagTally.EMPTY.countOf("wood"));
  }

  @Test
  void anItemIsNotCountedTwiceForTheSameTag() {
    final NetworkTagTally tally =
        NetworkTagTally.of(
            List.of(new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood", "WOOD"))));
    assertEquals(1, tally.countOf("wood"));
  }
}
