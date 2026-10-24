package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.tag.PresetTag;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SortHoldingTest {
  private static SortHolding<String> holding(String variant, long count) {
    return holding(variant, count, TestCandidates.STACK);
  }

  private static SortHolding<String> holding(String variant, long count, int maxCount) {
    return new SortHolding<>(
        variant, TestCandidates.of(PresetTag.STONE, variant, "", variant, count, maxCount));
  }

  @Test
  void aHoldingReportsTheQuantityItsCandidateCarries() {
    assertEquals(17L, holding("minecraft:stone", 17L).count());
  }

  @Test
  void aHoldingOfNothingIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> holding("minecraft:stone", 0L));
  }

  @Test
  void aHoldingWithoutAVariantIsRejected() {
    assertThrows(
        NullPointerException.class,
        () -> new SortHolding<String>(null, TestCandidates.of(PresetTag.STONE, "a", 1L)));
  }

  @Test
  void aHoldingWithoutACandidateIsRejected() {
    assertThrows(NullPointerException.class, () -> new SortHolding<>("minecraft:stone", null));
  }

  @Test
  void mergingTwoHoldingsSumsTheirQuantities() {
    SortHolding<String> merged =
        holding("minecraft:stone", 20L).plus(holding("minecraft:stone", 30L));

    assertEquals(50L, merged.count());
  }

  @Test
  void mergingKeepsEverythingAboutTheHoldingExceptItsQuantity() {
    SortHolding<String> merged =
        holding("minecraft:stone", 20L).plus(holding("minecraft:stone", 30L));

    assertEquals("minecraft:stone", merged.variant());
    assertEquals(PresetTag.STONE, merged.candidate().primaryCategory());
    assertEquals(TestCandidates.STACK, merged.candidate().maxCount());
  }

  @Test
  void mergingHoldingsOfDifferentVariantsIsRejected() {
    SortHolding<String> stone = holding("minecraft:stone", 1L);
    SortHolding<String> dirt = holding("minecraft:dirt", 1L);

    assertThrows(IllegalArgumentException.class, () -> stone.plus(dirt));
  }

  @Test
  void mergingWithNothingIsRejected() {
    SortHolding<String> stone = holding("minecraft:stone", 1L);

    assertThrows(NullPointerException.class, () -> stone.plus(null));
  }

  @ParameterizedTest
  @CsvSource({"1,64,1", "64,64,1", "65,64,2", "128,64,2", "129,64,3", "16,16,1", "17,16,2"})
  void aHoldingBreaksIntoAsManyStacksAsItNeeds(long count, int maxCount, int expectedStacks) {
    assertEquals(expectedStacks, holding("minecraft:stone", count, maxCount).split().size());
  }

  @Test
  void theFullStacksComeFirstAndTheRemainderLast() {
    List<SortHolding<String>> stacks = holding("minecraft:stone", 150L, 64).split();

    assertEquals(List.of(64L, 64L, 22L), stacks.stream().map(SortHolding::count).toList());
  }

  @ParameterizedTest
  @CsvSource({"1,64", "63,64", "150,64", "999,16", "1,1", "7,3"})
  void splittingNeverLosesOrInventsAnything(long count, int maxCount) {
    List<SortHolding<String>> stacks = holding("minecraft:stone", count, maxCount).split();

    assertEquals(count, stacks.stream().mapToLong(SortHolding::count).sum());
  }

  @Test
  void everyStackFromASplitKeepsTheVariantItCameFrom() {
    List<SortHolding<String>> stacks = holding("minecraft:stone", 150L).split();

    assertEquals(
        List.of("minecraft:stone", "minecraft:stone", "minecraft:stone"),
        stacks.stream().map(SortHolding::variant).toList());
  }

  @Test
  void aSplitHoldingCannotBeChangedByCallers() {
    List<SortHolding<String>> stacks = holding("minecraft:stone", 150L).split();

    assertThrows(
        UnsupportedOperationException.class, () -> stacks.add(holding("minecraft:dirt", 1L)));
  }
}
