package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.SortOrdering;
import com.grahambartley.totalstorage.tag.PresetTag;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SortLayoutTest {
  private static final Comparator<SortCandidate> BY_REGISTRY_ID =
      SortComparator.forOrdering(SortOrdering.REGISTRY_ID);

  private static SortHolding<String> holding(PresetTag category, String variant, long count) {
    return holding(category, variant, count, TestCandidates.STACK);
  }

  private static SortHolding<String> holding(
      PresetTag category, String variant, long count, int maxCount) {
    return new SortHolding<>(
        variant, TestCandidates.of(category, variant, "", variant, count, maxCount));
  }

  private static List<String> variantsOf(List<SortHolding<String>> arranged) {
    return arranged.stream().map(SortHolding::variant).toList();
  }

  private static List<Long> countsOf(List<SortHolding<String>> arranged) {
    return arranged.stream().map(SortHolding::count).toList();
  }

  @Test
  void anEmptyContainerArrangesIntoNothing() {
    assertEquals(List.of(), SortLayout.arrange(List.of(), BY_REGISTRY_ID));
  }

  @Test
  void scatteredPartStacksOfOneItemBecomeOneStack() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.STONE, "minecraft:cobblestone", 10L),
                holding(PresetTag.STONE, "minecraft:cobblestone", 20L),
                holding(PresetTag.STONE, "minecraft:cobblestone", 30L)),
            BY_REGISTRY_ID);

    assertEquals(1, arranged.size());
    assertEquals(60L, arranged.get(0).count());
  }

  @Test
  void mergingOverflowsIntoAFullStackAndARemainder() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.STONE, "minecraft:cobblestone", 60L),
                holding(PresetTag.STONE, "minecraft:cobblestone", 30L)),
            BY_REGISTRY_ID);

    assertEquals(List.of(64L, 26L), countsOf(arranged));
  }

  @Test
  void differentVariantsAreNeverMergedTogether() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.STONE, "minecraft:cobblestone", 10L),
                holding(PresetTag.STONE, "minecraft:andesite", 10L)),
            BY_REGISTRY_ID);

    assertEquals(2, arranged.size());
    assertEquals(List.of(10L, 10L), countsOf(arranged));
  }

  @Test
  void theArrangementFollowsTheComparatorRatherThanTheOrderItWasGiven() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.STONE, "minecraft:cobblestone", 1L),
                holding(PresetTag.STONE, "minecraft:andesite", 1L)),
            BY_REGISTRY_ID);

    assertEquals(List.of("minecraft:andesite", "minecraft:cobblestone"), variantsOf(arranged));
  }

  @Test
  void categoryOutranksIdentifierSoItemsStayGroupedByPreset() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.STONE, "minecraft:andesite", 1L),
                holding(PresetTag.WOOD, "minecraft:acacia_log", 1L)),
            BY_REGISTRY_ID);

    assertEquals(
        List.of(PresetTag.WOOD, PresetTag.STONE),
        arranged.stream().map(holding -> holding.candidate().primaryCategory()).toList());
  }

  @Test
  void anAlreadySortedContainerArrangesToTheSameThing() {
    List<SortHolding<String>> sorted =
        List.of(
            holding(PresetTag.STONE, "minecraft:andesite", 64L),
            holding(PresetTag.STONE, "minecraft:cobblestone", 64L));

    assertEquals(variantsOf(sorted), variantsOf(SortLayout.arrange(sorted, BY_REGISTRY_ID)));
    assertEquals(countsOf(sorted), countsOf(SortLayout.arrange(sorted, BY_REGISTRY_ID)));
  }

  @Test
  void arrangingIsIdempotent() {
    List<SortHolding<String>> messy =
        List.of(
            holding(PresetTag.STONE, "minecraft:cobblestone", 40L),
            holding(PresetTag.WOOD, "minecraft:acacia_log", 5L),
            holding(PresetTag.STONE, "minecraft:cobblestone", 40L));

    List<SortHolding<String>> once = SortLayout.arrange(messy, BY_REGISTRY_ID);
    List<SortHolding<String>> twice = SortLayout.arrange(once, BY_REGISTRY_ID);

    assertEquals(variantsOf(once), variantsOf(twice));
    assertEquals(countsOf(once), countsOf(twice));
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void everyOrderingConservesTheTotal(SortOrdering ordering) {
    List<SortHolding<String>> messy =
        List.of(
            holding(PresetTag.STONE, "minecraft:cobblestone", 40L),
            holding(PresetTag.WOOD, "minecraft:acacia_log", 5L),
            holding(PresetTag.STONE, "minecraft:cobblestone", 40L),
            holding(PresetTag.FOOD, "minecraft:golden_apple", 7L, 16),
            holding(PresetTag.FOOD, "minecraft:golden_apple", 30L, 16));

    List<SortHolding<String>> arranged =
        SortLayout.arrange(messy, SortComparator.forOrdering(ordering));

    assertEquals(SortLayout.totalOf(messy), SortLayout.totalOf(arranged));
    assertEquals(122L, SortLayout.totalOf(arranged));
  }

  @Test
  void aMergedHoldingRespectsItsOwnStackLimitRatherThanSixtyFour() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(
                holding(PresetTag.FOOD, "minecraft:golden_apple", 10L, 16),
                holding(PresetTag.FOOD, "minecraft:golden_apple", 10L, 16)),
            BY_REGISTRY_ID);

    assertEquals(List.of(16L, 4L), countsOf(arranged));
  }

  @Test
  void arrangingNeverNeedsMoreStacksThanItWasGiven() {
    List<SortHolding<String>> messy =
        List.of(
            holding(PresetTag.STONE, "minecraft:cobblestone", 1L),
            holding(PresetTag.STONE, "minecraft:cobblestone", 1L),
            holding(PresetTag.STONE, "minecraft:cobblestone", 1L),
            holding(PresetTag.WOOD, "minecraft:acacia_log", 64L));

    assertTrue(SortLayout.arrange(messy, BY_REGISTRY_ID).size() <= messy.size());
  }

  @Test
  void anArrangementCannotBeChangedByCallers() {
    List<SortHolding<String>> arranged =
        SortLayout.arrange(
            List.of(holding(PresetTag.STONE, "minecraft:cobblestone", 1L)), BY_REGISTRY_ID);

    assertThrows(
        UnsupportedOperationException.class,
        () -> arranged.add(holding(PresetTag.STONE, "minecraft:andesite", 1L)));
  }

  @Test
  void arrangingWithoutHoldingsIsRejected() {
    assertThrows(NullPointerException.class, () -> SortLayout.arrange(null, BY_REGISTRY_ID));
  }

  @Test
  void arrangingWithoutAComparatorIsRejected() {
    assertThrows(NullPointerException.class, () -> SortLayout.arrange(List.of(), null));
  }

  @Test
  void theTotalOfNothingIsZero() {
    assertEquals(0L, SortLayout.totalOf(List.of()));
  }
}
