package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.SortOrdering;
import com.grahambartley.toomanychests.tag.PresetTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SortComparatorTest {
  private static final Comparator<SortCandidate> BY_REGISTRY_ID =
      SortComparator.forOrdering(SortOrdering.REGISTRY_ID);
  private static final Comparator<SortCandidate> BY_DISPLAY_NAME =
      SortComparator.forOrdering(SortOrdering.DISPLAY_NAME);

  private static List<SortCandidate> sorted(
      Comparator<SortCandidate> ordering, List<SortCandidate> candidates) {
    List<SortCandidate> copy = new ArrayList<>(candidates);
    copy.sort(ordering);
    return copy;
  }

  private static List<String> identifiers(List<SortCandidate> candidates) {
    return candidates.stream().map(SortCandidate::identifier).toList();
  }

  @Test
  void categoryGroupsItemsBeforeAnythingElseDoes() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.of(PresetTag.FOOD, "minecraft:apple", 1L),
            TestCandidates.of(PresetTag.WOOD, "minecraft:zzz_plank", 1L),
            TestCandidates.of(PresetTag.MISCELLANEOUS, "minecraft:aaa_widget", 1L),
            TestCandidates.of(PresetTag.BUILDING_BLOCKS, "minecraft:mmm_brick", 1L));

    assertEquals(
        List.of(
            "minecraft:mmm_brick",
            "minecraft:zzz_plank",
            "minecraft:apple",
            "minecraft:aaa_widget"),
        identifiers(sorted(BY_REGISTRY_ID, candidates)));
  }

  @Test
  void categoriesFollowTheOrderTheFilterColumnPresentsThem() {
    List<SortCandidate> shuffled = new ArrayList<>();
    for (PresetTag preset : PresetTag.all().reversed()) {
      shuffled.add(TestCandidates.of(preset, "minecraft:" + preset.id(), 1L));
    }

    assertEquals(
        PresetTag.all(),
        sorted(BY_REGISTRY_ID, shuffled).stream().map(SortCandidate::primaryCategory).toList());
  }

  @Test
  void identifiersOrderAscendingWithinACategory() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 1L),
            TestCandidates.of(PresetTag.STONE, "minecraft:andesite", 1L),
            TestCandidates.of(PresetTag.STONE, "create:limestone", 1L));

    assertEquals(
        List.of("create:limestone", "minecraft:andesite", "minecraft:stone"),
        identifiers(sorted(BY_REGISTRY_ID, candidates)));
  }

  @Test
  void componentsClusterVariantsOfOneItemWhenIdentifiersTie() {
    SortCandidate plain = variant("", 1L);
    SortCandidate sharpness = variant("enchantments=sharpness", 1L);
    SortCandidate unbreaking = variant("enchantments=unbreaking", 1L);

    assertEquals(
        List.of(plain, sharpness, unbreaking),
        sorted(BY_REGISTRY_ID, List.of(unbreaking, plain, sharpness)));
  }

  @Test
  void fullStacksSortAheadOfPartialsOfTheSameItem() {
    SortCandidate full = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 64L);
    SortCandidate partial = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 12L);

    assertEquals(List.of(full, partial), sorted(BY_REGISTRY_ID, List.of(partial, full)));
  }

  @Test
  void partialsCollectAtTheEndOfTheirItemInDescendingSize() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 3L),
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 64L),
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 40L),
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 64L));

    assertEquals(
        List.of(64L, 64L, 40L, 3L),
        sorted(BY_REGISTRY_ID, candidates).stream().map(SortCandidate::count).toList());
  }

  @Test
  void aHoldingBeyondOneStackStillLeadsItsItem() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 12L),
            TestCandidates.of(PresetTag.STONE, "minecraft:stone", 4096L));

    assertEquals(
        List.of(4096L, 12L),
        sorted(BY_REGISTRY_ID, candidates).stream().map(SortCandidate::count).toList());
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void sortingAnAlreadySortedCollectionProducesZeroMoves(SortOrdering ordering) {
    Comparator<SortCandidate> comparator = SortComparator.forOrdering(ordering);
    List<SortCandidate> once = sorted(comparator, mixedBag());
    List<SortCandidate> twice = sorted(comparator, once);

    for (int slot = 0; slot < once.size(); slot++) {
      assertSame(once.get(slot), twice.get(slot), "holding moved out of slot " + slot);
    }
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void theOrderIsTheSameWhateverOrderTheHoldingsArrivedIn(SortOrdering ordering) {
    Comparator<SortCandidate> comparator = SortComparator.forOrdering(ordering);
    List<SortCandidate> expected = sorted(comparator, mixedBag());
    Random shuffles = new Random(20260822L);

    for (int attempt = 0; attempt < 50; attempt++) {
      List<SortCandidate> shuffled = new ArrayList<>(mixedBag());
      Collections.shuffle(shuffled, shuffles);

      assertEquals(expected, sorted(comparator, shuffled), "attempt " + attempt);
    }
  }

  @ParameterizedTest
  @MethodSource("languages")
  void theDefaultOrderIsIdenticalWhateverLanguageTheServerRunsIn(String language) {
    List<SortCandidate> translated =
        mixedBag().stream()
            .map(
                candidate ->
                    new SortCandidate(
                        candidate.categories(),
                        candidate.identifier(),
                        candidate.componentsKey(),
                        language + "-" + candidate.identifier().hashCode(),
                        candidate.count(),
                        candidate.maxCount()))
            .toList();

    assertEquals(
        identifiers(sorted(BY_REGISTRY_ID, mixedBag())),
        identifiers(sorted(BY_REGISTRY_ID, translated)));
  }

  static List<String> languages() {
    return List.of("en_us", "pt_br", "de_de", "ja_jp");
  }

  @Test
  void displayNameOrderingSortsByTheTranslatedNameWithinACategory() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.named(PresetTag.FOOD, "minecraft:cooked_beef", "Steak"),
            TestCandidates.named(PresetTag.FOOD, "minecraft:apple", "Apple"),
            TestCandidates.named(PresetTag.FOOD, "minecraft:bread", "Bread"));

    assertEquals(
        List.of("minecraft:apple", "minecraft:bread", "minecraft:cooked_beef"),
        identifiers(sorted(BY_DISPLAY_NAME, candidates)));
  }

  @Test
  void displayNameOrderingStillGroupsByCategoryFirst() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.named(PresetTag.FOOD, "minecraft:apple", "Apple"),
            TestCandidates.named(PresetTag.WOOD, "minecraft:oak_planks", "Zebra Planks"));

    assertEquals(
        List.of("minecraft:oak_planks", "minecraft:apple"),
        identifiers(sorted(BY_DISPLAY_NAME, candidates)));
  }

  @Test
  void displayNameOrderingFallsBackToTheIdentifierWhenTwoItemsShareAName() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.named(PresetTag.STONE, "create:stone", "Stone"),
            TestCandidates.named(PresetTag.STONE, "minecraft:stone", "Stone"),
            TestCandidates.named(PresetTag.STONE, "another:stone", "Stone"));

    assertEquals(
        List.of("another:stone", "create:stone", "minecraft:stone"),
        identifiers(sorted(BY_DISPLAY_NAME, candidates)));
  }

  @Test
  void theTwoOrderingsDisagreeWhereNamesAndIdentifiersDisagree() {
    List<SortCandidate> candidates =
        List.of(
            TestCandidates.named(PresetTag.FOOD, "minecraft:cooked_beef", "Steak"),
            TestCandidates.named(PresetTag.FOOD, "minecraft:melon_slice", "Melon Slice"));

    assertEquals(
        List.of("minecraft:cooked_beef", "minecraft:melon_slice"),
        identifiers(sorted(BY_REGISTRY_ID, candidates)));
    assertEquals(
        List.of("minecraft:melon_slice", "minecraft:cooked_beef"),
        identifiers(sorted(BY_DISPLAY_NAME, candidates)));
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void holdingsThatAgreeOnEveryKeyCompareEqualSoAStableSortLeavesThemAlone(SortOrdering ordering) {
    Comparator<SortCandidate> comparator = SortComparator.forOrdering(ordering);
    SortCandidate left = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 32L);
    SortCandidate right = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 32L);

    assertEquals(0, comparator.compare(left, right));
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void theOrderingIsAntisymmetricAcrossTheWholeBag(SortOrdering ordering) {
    Comparator<SortCandidate> comparator = SortComparator.forOrdering(ordering);
    List<SortCandidate> bag = mixedBag();

    for (SortCandidate left : bag) {
      for (SortCandidate right : bag) {
        assertEquals(
            Integer.signum(comparator.compare(left, right)),
            -Integer.signum(comparator.compare(right, left)),
            left + " against " + right);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void theSortedOrderNeverStepsBackwardsUnderTheComparator(SortOrdering ordering) {
    Comparator<SortCandidate> comparator = SortComparator.forOrdering(ordering);
    List<SortCandidate> bag = sorted(comparator, mixedBag());

    for (int first = 0; first < bag.size(); first++) {
      for (int second = first; second < bag.size(); second++) {
        assertTrue(
            comparator.compare(bag.get(first), bag.get(second)) <= 0,
            bag.get(first) + " should not follow " + bag.get(second));
      }
    }
  }

  @Test
  void aNullOrderingIsRejected() {
    assertThrows(NullPointerException.class, () -> SortComparator.forOrdering(null));
  }

  private static SortCandidate variant(String componentsKey, long count) {
    return TestCandidates.of(
        PresetTag.WEAPONS_AND_ARMOUR,
        "minecraft:diamond_sword",
        componentsKey,
        "Diamond Sword",
        count,
        1);
  }

  private static List<SortCandidate> mixedBag() {
    return List.of(
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", 64L),
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", 12L),
        TestCandidates.of(PresetTag.STONE, "minecraft:andesite", 64L),
        TestCandidates.of(PresetTag.WOOD, "minecraft:oak_planks", 64L),
        TestCandidates.of(PresetTag.WOOD, "minecraft:oak_log", 5L),
        TestCandidates.named(PresetTag.FOOD, "minecraft:cooked_beef", "Steak"),
        TestCandidates.named(PresetTag.FOOD, "minecraft:apple", "Apple"),
        variant("", 1L),
        variant("enchantments=sharpness", 1L),
        variant("damage=12", 1L),
        TestCandidates.of(PresetTag.MISCELLANEOUS, "some_mod:mystery_widget", 1L),
        TestCandidates.of(PresetTag.BUILDING_BLOCKS, "create:brass_block", 30L));
  }
}
