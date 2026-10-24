package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagSkipsTest {
  @Test
  void nothingSkippedIsEmptyAndCountsNothing() {
    assertTrue(TagSkips.NONE.isEmpty());
    assertEquals(0, TagSkips.NONE.total());
    assertEquals(0, TagSkips.NONE.countOf(TagSkipReason.MALFORMED));
  }

  @ParameterizedTest
  @EnumSource(TagSkipReason.class)
  void aReasonCountsUnderItselfAndNowhereElse(TagSkipReason reason) {
    final TagSkips skips = TagSkips.of(reason, 3);

    assertEquals(3, skips.countOf(reason));
    assertEquals(3, skips.total());
  }

  @Test
  void theSameReasonTwiceAddsUpRatherThanReplacing() {
    final TagSkips skips = TagSkips.of(TagSkipReason.MALFORMED, 2).and(TagSkipReason.MALFORMED);

    assertEquals(3, skips.countOf(TagSkipReason.MALFORMED));
  }

  @Test
  void theTotalIsEveryReasonAddedTogether() {
    final TagSkips skips =
        TagSkips.of(TagSkipReason.MALFORMED, 2)
            .and(TagSkipReason.TOO_LONG)
            .and(TagSkipReason.PRESET_NAME);

    assertEquals(4, skips.total());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void aCountThatIsNotASkipIsNotRecorded(int count) {
    assertTrue(TagSkips.of(TagSkipReason.MALFORMED, count).isEmpty());
  }

  @Test
  void aReasonWithNoReasonIsNotRecorded() {
    assertTrue(TagSkips.NONE.and(null, 3).isEmpty());
  }

  @Test
  void atallyBuiltFromNothingIsTheSameAsNothing() {
    assertTrue(new TagSkips(null).isEmpty());
    assertTrue(new TagSkips(Map.of()).isEmpty());
  }

  @Test
  void anEmptyOrNullEntryNeverSurvivesIntoTheTally() {
    final Map<TagSkipReason, Integer> raw = new HashMap<>();
    raw.put(TagSkipReason.MALFORMED, 0);
    raw.put(TagSkipReason.TOO_LONG, null);
    raw.put(TagSkipReason.NO_ROOM, 2);

    final TagSkips skips = new TagSkips(raw);

    assertEquals(2, skips.total());
    assertEquals(2, skips.countOf(TagSkipReason.NO_ROOM));
  }

  @Test
  void atallyCannotBeGrownThroughTheMapItHandsBack() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> TagSkips.of(TagSkipReason.MALFORMED, 1).byReason().put(TagSkipReason.NO_ROOM, 1));
  }

  @Test
  void addingNothingToATallyLeavesItAlone() {
    final TagSkips skips = TagSkips.of(TagSkipReason.MALFORMED, 1);

    assertEquals(skips, skips.and(TagSkipReason.TOO_LONG, 0));
    assertFalse(skips.isEmpty());
  }
}
