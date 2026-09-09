package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FilterSelectionTest {
  @Test
  void aFreshSelectionFiltersNothingAndCombinesWithAnd() {
    final FilterSelection selection = new FilterSelection();
    assertFalse(selection.isFiltering());
    assertEquals(Set.of(), selection.active());
    assertEquals(TagCombinationMode.AND, selection.mode());
  }

  @Test
  void togglingATagOnAndOffAgainLeavesNothingSelected() {
    final FilterSelection selection = new FilterSelection();
    assertTrue(selection.toggle("wood"));
    assertTrue(selection.isActive("wood"));
    assertTrue(selection.toggle("wood"));
    assertFalse(selection.isActive("wood"));
    assertFalse(selection.isFiltering());
  }

  @ParameterizedTest
  @ValueSource(strings = {"WOOD", "  wood  ", "Wood"})
  void aTagIsMatchedWhateverCaseOrPaddingItArrivesIn(String asked) {
    final FilterSelection selection = new FilterSelection();
    selection.toggle("wood");
    assertTrue(selection.isActive(asked));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aBlankTagIsRefusedRatherThanSelected(String tag) {
    final FilterSelection selection = new FilterSelection();
    assertFalse(selection.toggle(tag));
    assertFalse(selection.isFiltering());
  }

  @Test
  void severalTagsCanNarrowTogether() {
    final FilterSelection selection = new FilterSelection();
    selection.toggle("wood");
    selection.toggle("building");
    assertEquals(Set.of("wood", "building"), selection.active());
  }

  @Test
  void clearingReportsWhetherItHadAnythingToClear() {
    final FilterSelection selection = new FilterSelection();
    assertFalse(selection.clear());
    selection.toggle("wood");
    assertTrue(selection.clear());
    assertFalse(selection.isFiltering());
  }

  @Test
  void theModeCyclesBetweenNarrowingAndBroadening() {
    final FilterSelection selection = new FilterSelection();
    assertEquals(TagCombinationMode.OR, selection.cycleMode());
    assertEquals(TagCombinationMode.OR, selection.mode());
    assertEquals(TagCombinationMode.AND, selection.cycleMode());
  }

  @Test
  void aTagTheNetworkNoLongerHasIsDroppedRatherThanFilteringOnNothing() {
    final FilterSelection selection = new FilterSelection();
    selection.toggle("wood");
    selection.toggle("castle");
    selection.retainAll(Set.of("wood", "stone"));
    assertEquals(Set.of("wood"), selection.active());
  }

  @Test
  void selectingEveryTagANetworkCanOfferIsAllowed() {
    final FilterSelection selection = new FilterSelection();

    for (int tag = 0; tag < NetworkTagSet.MAX_TOTAL; tag++) {
      assertTrue(selection.toggle("tag" + tag), "tag " + tag + " should have been selectable");
    }

    assertEquals(NetworkTagSet.MAX_TOTAL, selection.active().size());
  }

  @Test
  void aTagPastTheCapIsRefusedRatherThanSentInAPacketTheServerRejects() {
    final FilterSelection selection = new FilterSelection();
    for (int tag = 0; tag < NetworkTagSet.MAX_TOTAL; tag++) {
      selection.toggle("tag" + tag);
    }

    assertFalse(selection.toggle("one too many"));
    assertEquals(NetworkTagSet.MAX_TOTAL, selection.active().size());
    assertFalse(selection.isActive("one too many"));
  }

  @Test
  void clearingATagAtTheCapMakesRoomAgain() {
    final FilterSelection selection = new FilterSelection();
    for (int tag = 0; tag < NetworkTagSet.MAX_TOTAL; tag++) {
      selection.toggle("tag" + tag);
    }

    assertTrue(selection.toggle("tag0"));
    assertTrue(selection.toggle("one more"));
  }

  @Test
  void theActiveSetIsNotEditableFromOutside() {
    final FilterSelection selection = new FilterSelection();
    selection.toggle("wood");
    assertThrows(UnsupportedOperationException.class, () -> selection.active().add("stone"));
  }
}
