package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexBuilder;
import com.grahambartley.toomanychests.index.NetworkIndexEntry;
import com.grahambartley.toomanychests.index.NetworkIndexOrdering;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class NetworkIndexFilterTest {
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

  private static NetworkIndex<String> index() {
    return new NetworkIndexBuilder<String>()
        .add("cobblestone", 300L, ORIGIN)
        .add("dark_oak_chest", 8L, ORIGIN)
        .add("iron_pickaxe", 1L, ORIGIN)
        .add("cogwheel", 64L, ORIGIN)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static List<String> variantsMatching(ItemFilter filter) {
    return NetworkIndexFilter.apply(index(), TO_CANDIDATE, filter).stream()
        .map(NetworkIndexEntry::variant)
        .toList();
  }

  @Test
  void filteringKeepsIndexOrderRatherThanMatchOrder() {
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

    assertSame(index.entries(), NetworkIndexFilter.apply(index, TO_CANDIDATE, ItemFilter.NONE));
  }

  @Test
  void aFilterMatchingNothingReturnsEmptyRatherThanTheWholeIndex() {
    assertTrue(variantsMatching(ItemFilter.ofSearch("@nonexistent_mod")).isEmpty());
  }

  @Test
  void aggregatedCountsSurviveFiltering() {
    List<NetworkIndexEntry<String>> matches =
        NetworkIndexFilter.apply(
            index(), TO_CANDIDATE, ItemFilter.ofTags(TagCombinationMode.AND, Set.of("stone")));

    assertEquals(1, matches.size());
    assertEquals(300L, matches.get(0).totalCount());
    assertEquals(List.of(ORIGIN), matches.get(0).origins());
  }

  @Test
  void anEmptyIndexFiltersToNothing() {
    assertTrue(
        NetworkIndexFilter.apply(
                NetworkIndex.<String>empty(), TO_CANDIDATE, ItemFilter.ofSearch("iron"))
            .isEmpty());
  }

  @Test
  void nullArgumentsAreRejectedRatherThanSilentlyMatching() {
    assertThrows(
        NullPointerException.class,
        () -> NetworkIndexFilter.apply(null, TO_CANDIDATE, ItemFilter.NONE));
    assertThrows(
        NullPointerException.class, () -> NetworkIndexFilter.apply(index(), null, ItemFilter.NONE));
    assertThrows(
        NullPointerException.class, () -> NetworkIndexFilter.apply(index(), TO_CANDIDATE, null));
  }
}
