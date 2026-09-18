package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TagSetContentsTest {
  private static final NetworkTag CASTLE = NetworkTag.custom("castle", "Castle", Set.of());

  @Test
  void contentsCarryOnlyTheCustomTagsOfASetBecausePresetsAreWorkedOut() {
    final TagSetContents contents =
        TagSetContents.of(NetworkTagSet.defaults().withCustomTag("castle"));

    assertEquals(1, contents.tags().size());
    assertEquals("castle", contents.tags().get(0).id());
    assertEquals(0, contents.malformed());
  }

  @Test
  void contentsKeepEveryEntryTheyWereHandedRatherThanCanonicalisingThemAway() {
    final List<NetworkTag> raw =
        List.of(CASTLE, NetworkTag.custom(PresetTag.TOOLS.id(), "Tools", Set.of()));

    assertEquals(2, new TagSetContents(raw, 0).tags().size());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void aMalformedCountBelowNothingIsTreatedAsNothing(int malformed) {
    assertEquals(0, new TagSetContents(List.of(CASTLE), malformed).malformed());
  }

  @Test
  void contentsWithoutTagsAreEmptyRatherThanBroken() {
    assertTrue(new TagSetContents(null, 0).tags().isEmpty());
    assertTrue(TagSetContents.of(null).tags().isEmpty());
  }

  @Test
  void contentsCannotBeGrownThroughTheListTheyHandBack() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> new TagSetContents(List.of(CASTLE), 0).tags().add(CASTLE));
  }
}
