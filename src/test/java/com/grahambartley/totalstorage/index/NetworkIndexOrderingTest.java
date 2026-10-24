package com.grahambartley.totalstorage.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class NetworkIndexOrderingTest {
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);
  private static final Comparator<NetworkIndexEntry<String>> BY_NAME =
      NetworkIndexOrdering.byKey(Function.identity());

  private static NetworkIndexEntry<String> entry(String variant, long count, int providers) {
    return new NetworkIndexEntry<>(variant, count, List.of(TestOrigins.chain(providers)));
  }

  private static List<String> sorted(
      Comparator<NetworkIndexEntry<String>> ordering, List<NetworkIndexEntry<String>> entries) {
    List<NetworkIndexEntry<String>> copy = new ArrayList<>(entries);
    copy.sort(ordering);
    return copy.stream().map(NetworkIndexEntry::variant).toList();
  }

  @Test
  void entriesOrderByTheirSortKeyAscending() {
    List<NetworkIndexEntry<String>> entries =
        List.of(entry("torch", 1L, 1), entry("cobblestone", 1L, 1), entry("diamond", 1L, 1));

    assertEquals(List.of("cobblestone", "diamond", "torch"), sorted(BY_NAME, entries));
  }

  @Test
  void theLargerHoldingComesFirstWhenSortKeysTie() {
    List<NetworkIndexEntry<String>> entries =
        List.of(entry("sword", 1L, 1), entry("sword", 9L, 1), entry("sword", 5L, 1));

    Comparator<NetworkIndexEntry<String>> ordering = NetworkIndexOrdering.byKey(variant -> "sword");
    List<NetworkIndexEntry<String>> copy = new ArrayList<>(entries);
    copy.sort(ordering);

    assertEquals(List.of(9L, 5L, 1L), copy.stream().map(NetworkIndexEntry::totalCount).toList());
  }

  @Test
  void theMoreWidelySpreadComesFirstWhenSortKeyAndCountTie() {
    List<NetworkIndexEntry<String>> entries =
        List.of(entry("stone", 64L, 1), entry("stone", 64L, 3));

    Comparator<NetworkIndexEntry<String>> ordering = NetworkIndexOrdering.byKey(variant -> "stone");
    List<NetworkIndexEntry<String>> copy = new ArrayList<>(entries);
    copy.sort(ordering);

    assertEquals(List.of(3, 1), copy.stream().map(NetworkIndexEntry::providerCount).toList());
  }

  @Test
  void sortingAnAlreadySortedIndexChangesNothing() {
    List<NetworkIndexEntry<String>> entries =
        List.of(entry("torch", 1L, 1), entry("cobblestone", 1L, 1), entry("diamond", 1L, 1));

    List<String> once = sorted(BY_NAME, entries);
    List<NetworkIndexEntry<String>> ordered = new ArrayList<>(entries);
    ordered.sort(BY_NAME);

    assertEquals(once, sorted(BY_NAME, ordered));
  }

  @Test
  void theSameInputAlwaysProducesTheSameOrder() {
    List<NetworkIndexEntry<String>> entries =
        List.of(
            entry("torch", 5L, 1),
            entry("cobblestone", 300L, 2),
            entry("diamond", 12L, 1),
            entry("cobblestone", 64L, 1));

    assertEquals(sorted(BY_NAME, entries), sorted(BY_NAME, entries));
  }

  @Test
  void containersHoldingTheSameVariantDoNotAffectOrderingOfOthers() {
    NetworkIndex<String> first =
        new NetworkIndexBuilder<String>()
            .add("stone", 64L, FIRST)
            .add("dirt", 64L, SECOND)
            .build(BY_NAME);
    NetworkIndex<String> second =
        new NetworkIndexBuilder<String>()
            .add("dirt", 64L, SECOND)
            .add("stone", 64L, FIRST)
            .build(BY_NAME);

    assertEquals(
        first.entries().stream().map(NetworkIndexEntry::variant).toList(),
        second.entries().stream().map(NetworkIndexEntry::variant).toList());
  }

  @Test
  void aNullSortKeyFunctionIsRejected() {
    assertThrows(NullPointerException.class, () -> NetworkIndexOrdering.byKey(null));
  }

  @Test
  void aSortKeyThatReturnsNullIsRejected() {
    Comparator<NetworkIndexEntry<String>> ordering = NetworkIndexOrdering.byKey(variant -> null);
    NetworkIndexEntry<String> left = entry("stone", 1L, 1);
    NetworkIndexEntry<String> right = entry("dirt", 1L, 1);

    assertThrows(NullPointerException.class, () -> ordering.compare(left, right));
  }
}
