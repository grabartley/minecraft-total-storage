package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexBuilder;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.index.NetworkIndexOrdering;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class NetworkIndexSearchTest {
  private static final int LARGE_NETWORK_SIZE = 5_000;
  private static final int WARMUP_PASSES = 20;
  private static final int MEASURED_PASSES = 10;
  private static final long TICK_BUDGET_MILLIS = 50L;

  private static final StorageOrigin ORIGIN =
      new BlockStorageOrigin(
          RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")),
          new BlockPos(0, 64, 0));

  private static final Map<String, FilterCandidate> CANDIDATES =
      Map.of(
          "cobblestone", TestCandidates.COBBLESTONE,
          "dark_oak_chest", TestCandidates.DARK_OAK_CHEST,
          "iron_pickaxe", TestCandidates.IRON_PICKAXE,
          "cogwheel", TestCandidates.COGWHEEL);

  private static final Function<String, FilterCandidate> TO_CANDIDATE = CANDIDATES::get;

  private static final Map<String, FilterCandidate> CHEST_CANDIDATES =
      Map.of(
          "cherry_chest_boat",
              new FilterCandidate(
                  "minecraft:cherry_chest_boat", "Cherry Boat with Chest", Set.of("wood")),
          "chest", new FilterCandidate("minecraft:chest", "Chest", Set.of("wood")),
          "chest_minecart",
              new FilterCandidate("minecraft:chest_minecart", "Minecart with Chest", Set.of()),
          "ender_chest", new FilterCandidate("minecraft:ender_chest", "Ender Chest", Set.of()));

  private static NetworkIndex<String> index() {
    return new NetworkIndexBuilder<String>()
        .add("cobblestone", 300L, ORIGIN)
        .add("dark_oak_chest", 8L, ORIGIN)
        .add("iron_pickaxe", 1L, ORIGIN)
        .add("cogwheel", 64L, ORIGIN)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static NetworkIndex<String> chests() {
    return new NetworkIndexBuilder<String>()
        .add("cherry_chest_boat", 1L, ORIGIN)
        .add("chest", 12L, ORIGIN)
        .add("chest_minecart", 1L, ORIGIN)
        .add("ender_chest", 2L, ORIGIN)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static List<String> chestsMatching(String search) {
    return NetworkIndexSearch.matching(chests(), CHEST_CANDIDATES::get, ItemFilter.ofSearch(search))
        .stream()
        .map(NetworkIndexEntry::variant)
        .toList();
  }

  private static List<String> variantsMatching(ItemFilter filter) {
    return NetworkIndexSearch.matching(index(), TO_CANDIDATE, filter).stream()
        .map(NetworkIndexEntry::variant)
        .toList();
  }

  @Test
  void theClosestAnswersComeFirstAndEqualOnesKeepIndexOrder() {
    assertEquals(
        List.of("chest", "chest_minecart", "cherry_chest_boat", "ender_chest"),
        chestsMatching("chest"));
  }

  @Test
  void aQueryWithNoTextToRankLeavesTheIndexOrderAlone() {
    assertEquals(List.of("cherry_chest_boat", "chest"), chestsMatching("#wood @minecraft"));
  }

  @Test
  void aPrefixOfTheIdentifierOutranksASubstringOfTheDisplayName() {
    assertEquals(List.of("chest_minecart", "cherry_chest_boat"), chestsMatching("chest_"));
  }

  @Test
  void narrowingByTagKeepsIndexOrderRatherThanMatchOrder() {
    assertEquals(
        List.of("cogwheel", "dark_oak_chest"),
        variantsMatching(ItemFilter.ofTags(TagCombinationMode.OR, Set.of("wood"))));
  }

  @Test
  void aSearchNarrowsTheIndexToItsMatches() {
    assertEquals(List.of("iron_pickaxe"), variantsMatching(ItemFilter.ofSearch("#tools")));
  }

  @Test
  void anEmptyFilterReturnsTheIndexUntouched() {
    NetworkIndex<String> index = index();

    assertSame(index.entries(), NetworkIndexSearch.matching(index, TO_CANDIDATE, ItemFilter.NONE));
  }

  @Test
  void aFilterMatchingNothingReturnsEmptyRatherThanTheWholeIndex() {
    assertTrue(variantsMatching(ItemFilter.ofSearch("@nonexistent_mod")).isEmpty());
  }

  @Test
  void aggregatedCountsSurviveFiltering() {
    List<NetworkIndexEntry<String>> matches =
        NetworkIndexSearch.matching(
            index(), TO_CANDIDATE, ItemFilter.ofTags(TagCombinationMode.AND, Set.of("stone")));

    assertEquals(1, matches.size());
    assertEquals(300L, matches.get(0).totalCount());
    assertEquals(List.of(ORIGIN), matches.get(0).origins());
  }

  @Test
  void anEmptyIndexFiltersToNothing() {
    assertTrue(
        NetworkIndexSearch.matching(
                NetworkIndex.<String>empty(), TO_CANDIDATE, ItemFilter.ofSearch("iron"))
            .isEmpty());
  }

  @Test
  void nullArgumentsAreRejectedRatherThanSilentlyMatching() {
    assertThrows(
        NullPointerException.class,
        () -> NetworkIndexSearch.matching(null, TO_CANDIDATE, ItemFilter.NONE));
    assertThrows(
        NullPointerException.class,
        () -> NetworkIndexSearch.matching(index(), null, ItemFilter.NONE));
    assertThrows(
        NullPointerException.class, () -> NetworkIndexSearch.matching(index(), TO_CANDIDATE, null));
  }

  private static NetworkIndex<String> largeNetwork() {
    final NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();
    for (int i = 0; i < LARGE_NETWORK_SIZE; i++) {
      builder.add("item_" + i, i + 1L, ORIGIN);
    }
    return builder.build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static Function<String, FilterCandidate> generatedCandidates() {
    final Map<String, FilterCandidate> candidates = new HashMap<>(LARGE_NETWORK_SIZE);
    for (int i = 0; i < LARGE_NETWORK_SIZE; i++) {
      final String variant = "item_" + i;
      candidates.put(
          variant,
          new FilterCandidate(
              "mod_" + (i % 20) + ":" + variant,
              "Generated Item " + i,
              Set.of("tag_" + (i % 13), "group_" + (i % 7))));
    }
    return candidates::get;
  }

  @Test
  void searchingAWholeNetworkFitsInsideATick() {
    final NetworkIndex<String> network = largeNetwork();
    final Function<String, FilterCandidate> toCandidate = generatedCandidates();
    final ItemFilter filter =
        new ItemFilter(
            Set.of("tag_3"), TagCombinationMode.AND, SearchQueryParser.parse("@mod_1 item #tag"));

    for (int warmup = 0; warmup < WARMUP_PASSES; warmup++) {
      NetworkIndexSearch.matching(network, toCandidate, filter);
    }

    long fastestNanos = Long.MAX_VALUE;
    for (int pass = 0; pass < MEASURED_PASSES; pass++) {
      final long startedAt = System.nanoTime();
      final List<NetworkIndexEntry<String>> matches =
          NetworkIndexSearch.matching(network, toCandidate, filter);
      fastestNanos = Math.min(fastestNanos, System.nanoTime() - startedAt);
      assertFalse(matches.isEmpty());
    }

    final long millis = TimeUnit.NANOSECONDS.toMillis(fastestNanos);
    assertTrue(
        millis < TICK_BUDGET_MILLIS,
        "searching " + LARGE_NETWORK_SIZE + " entries took " + millis + "ms");
  }
}
