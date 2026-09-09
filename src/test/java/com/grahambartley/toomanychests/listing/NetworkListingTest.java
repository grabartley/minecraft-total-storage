package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexBuilder;
import com.grahambartley.toomanychests.index.NetworkIndexOrdering;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class NetworkListingTest {
  private static final StorageOrigin ORIGIN =
      new BlockStorageOrigin(
          RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")),
          new BlockPos(0, 64, 0));

  private static final StorageOrigin SECOND_ORIGIN =
      new BlockStorageOrigin(
          RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")),
          new BlockPos(4, 64, 0));

  private static final Map<String, FilterCandidate> CANDIDATES = candidates();
  private static final Function<String, FilterCandidate> TO_CANDIDATE = CANDIDATES::get;

  private static Map<String, FilterCandidate> candidates() {
    final Map<String, FilterCandidate> byVariant = new LinkedHashMap<>();
    byVariant.put(
        "cobblestone",
        new FilterCandidate("minecraft:cobblestone", "Cobblestone", Set.of("building_blocks")));
    byVariant.put(
        "oak_planks",
        new FilterCandidate(
            "minecraft:oak_planks", "Oak Planks", Set.of("building_blocks", "wood")));
    byVariant.put("oak_log", new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood")));
    byVariant.put(
        "iron_pickaxe",
        new FilterCandidate("minecraft:iron_pickaxe", "Iron Pickaxe", Set.of("tools")));
    byVariant.put(
        "cooked_beef", new FilterCandidate("minecraft:cooked_beef", "Steak", Set.of("food")));
    return Map.copyOf(byVariant);
  }

  private static NetworkIndex<String> index() {
    return new NetworkIndexBuilder<String>()
        .add("cobblestone", 300L, ORIGIN)
        .add("cooked_beef", 12L, ORIGIN)
        .add("iron_pickaxe", 1L, ORIGIN)
        .add("oak_log", 64L, ORIGIN)
        .add("oak_planks", 128L, ORIGIN)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static ListingPage<String> page(ItemFilter filter, ListingWindow window, long revision) {
    return NetworkListing.page(index(), TO_CANDIDATE, filter, window, revision);
  }

  private static List<String> variantsOf(ListingPage<String> page) {
    return page.rows().stream().map(ListingRow::variant).toList();
  }

  @Test
  void aWindowOverTheWholeNetworkReturnsTheRowsItCovers() {
    ListingPage<String> page = page(ItemFilter.NONE, ListingWindow.of(1, 2), 4L);

    assertEquals(List.of("cooked_beef", "iron_pickaxe"), variantsOf(page));
    assertEquals(1, page.offset());
    assertEquals(4L, page.revision());
  }

  @Test
  void everyRowCarriesWhatTheWholeNetworkHoldsOfIt() {
    ListingPage<String> page = page(ItemFilter.NONE, ListingWindow.of(0, 1), 1L);

    assertEquals(new ListingRow<>("cobblestone", 300L, 1), page.rows().get(0));
  }

  @Test
  void everyRowCarriesHowManyContainersHoldIt() {
    ListingPage<String> page =
        NetworkListing.page(
            new NetworkIndexBuilder<String>()
                .add("cobblestone", 100L, ORIGIN)
                .add("cobblestone", 200L, SECOND_ORIGIN)
                .build(NetworkIndexOrdering.byKey(Function.identity())),
            TO_CANDIDATE,
            ItemFilter.NONE,
            ListingWindow.of(0, 10),
            1L);

    assertEquals(300L, page.rows().get(0).count());
    assertEquals(2, page.rows().get(0).providerCount());
  }

  @Test
  void theTotalCountsEveryMatchRatherThanTheRowsOnThePage() {
    ListingPage<String> page = page(ItemFilter.NONE, ListingWindow.of(0, 2), 1L);

    assertEquals(2, page.size());
    assertEquals(5, page.totalMatching());
  }

  @Test
  void filteringNarrowsTheTotalAsWellAsTheRows() {
    ListingPage<String> page =
        page(ItemFilter.ofTags(TagCombinationMode.OR, Set.of("wood")), ListingWindow.of(0, 10), 1L);

    assertEquals(List.of("oak_log", "oak_planks"), variantsOf(page));
    assertEquals(2, page.totalMatching());
  }

  @Test
  void aWindowIsTakenFromTheMatchesRatherThanFromTheWholeIndex() {
    ListingPage<String> page =
        page(ItemFilter.ofTags(TagCombinationMode.OR, Set.of("wood")), ListingWindow.of(1, 1), 1L);

    assertEquals(List.of("oak_planks"), variantsOf(page));
    assertEquals(1, page.offset());
    assertEquals(2, page.totalMatching());
  }

  @Test
  void searchingNarrowsTheListingTheSameWayFilteringDoes() {
    ListingPage<String> page = page(ItemFilter.ofSearch("oak"), ListingWindow.of(0, 10), 1L);

    assertEquals(List.of("oak_log", "oak_planks"), variantsOf(page));
    assertEquals(2, page.totalMatching());
  }

  @Test
  void aWindowPastTheEndOfTheMatchesIsAnEmptyPageThatStillCountsThem() {
    ListingPage<String> page = page(ItemFilter.NONE, ListingWindow.of(50, 10), 2L);

    assertTrue(page.isEmpty());
    assertEquals(5, page.totalMatching());
    assertEquals(50, page.offset());
  }

  @Test
  void criteriaMatchingNothingLeaveAnEmptyPageAndNothingToScrollThrough() {
    ListingPage<String> page = page(ItemFilter.ofSearch("diamond"), ListingWindow.of(0, 10), 1L);

    assertTrue(page.isEmpty());
    assertEquals(0, page.totalMatching());
  }

  @Test
  void anEmptyNetworkHasNothingToPage() {
    ListingPage<String> page =
        NetworkListing.page(
            NetworkIndex.empty(), TO_CANDIDATE, ItemFilter.NONE, ListingWindow.of(0, 10), 1L);

    assertTrue(page.isEmpty());
    assertEquals(0, page.totalMatching());
  }

  @Test
  void aPageWithRowsSaysTheListingIsLive() {
    assertEquals(ListingStatus.LIVE, page(ItemFilter.NONE, ListingWindow.of(0, 10), 1L).status());
  }

  @Test
  void criteriaThatMatchedNothingReadDifferentlyFromANetworkHoldingNothing() {
    assertEquals(
        ListingStatus.NO_RESULTS,
        page(ItemFilter.ofSearch("diamond"), ListingWindow.of(0, 10), 1L).status());
    assertEquals(
        ListingStatus.EMPTY_NETWORK,
        NetworkListing.page(
                NetworkIndex.empty(), TO_CANDIDATE, ItemFilter.NONE, ListingWindow.of(0, 10), 1L)
            .status());
  }

  @Test
  void scrollingPastTheEndIsStillALiveListing() {
    assertEquals(ListingStatus.LIVE, page(ItemFilter.NONE, ListingWindow.of(50, 10), 2L).status());
  }

  @Test
  void aPageIsNeverBuiltFromHalfTheInformationItNeeds() {
    assertThrows(
        NullPointerException.class,
        () -> NetworkListing.page(null, TO_CANDIDATE, ItemFilter.NONE, ListingWindow.EMPTY, 1L));
    assertThrows(
        NullPointerException.class,
        () -> NetworkListing.page(index(), null, ItemFilter.NONE, ListingWindow.EMPTY, 1L));
    assertThrows(
        NullPointerException.class,
        () -> NetworkListing.page(index(), TO_CANDIDATE, null, ListingWindow.EMPTY, 1L));
    assertThrows(
        NullPointerException.class,
        () -> NetworkListing.page(index(), TO_CANDIDATE, ItemFilter.NONE, null, 1L));
  }
}
