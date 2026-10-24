package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.tag.PresetTag;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SortPlanTest {
  private static SortHolding<String> holding(String variant, long count) {
    return new SortHolding<>(variant, TestCandidates.of(PresetTag.STONE, variant, count));
  }

  @Test
  void aPlanOfNothingIsEmptyAndHoldsNothing() {
    SortPlan<String> plan = SortPlan.nothing();

    assertTrue(plan.isEmpty());
    assertEquals(0, plan.size());
    assertEquals(0L, plan.total());
  }

  @Test
  void aPlanCountsTheStacksItArranged() {
    SortPlan<String> plan =
        new SortPlan<>(List.of(holding("cobblestone", 64L), holding("cobblestone", 10L)), 74L);

    assertFalse(plan.isEmpty());
    assertEquals(2, plan.size());
  }

  @Test
  void aPlanWhoseStacksDoNotAddUpToItsTotalIsRefused() {
    List<SortHolding<String>> stacks = List.of(holding("cobblestone", 20L));

    assertThrows(IllegalArgumentException.class, () -> new SortPlan<>(stacks, 21L));
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, -64L})
  void aPlanCannotClaimANegativeTotal(long total) {
    assertThrows(IllegalArgumentException.class, () -> new SortPlan<String>(List.of(), total));
  }

  @Test
  void aPlanRefusesToBeBuiltWithoutStacks() {
    assertThrows(NullPointerException.class, () -> new SortPlan<String>(null, 0L));
  }

  @Test
  void aPlanCopiesTheStacksItWasGiven() {
    List<SortHolding<String>> stacks = new java.util.ArrayList<>(List.of(holding("coal", 5L)));
    SortPlan<String> plan = new SortPlan<>(stacks, 5L);
    stacks.clear();

    assertEquals(1, plan.size());
  }
}
