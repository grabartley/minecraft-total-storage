package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FuzzySearchAttachmentTest {
  private static final FilterCandidate PICKAXE =
      new FilterCandidate("minecraft:diamond_pickaxe", "Diamond Pickaxe", Set.of("tools"));

  @ParameterizedTest
  @CsvSource({
    "diamond_pickaxe,EXACT",
    "diamond,PREFIX",
    "pick,SUBSTRING",
    "dpick,NONE",
    "dmndpckx,NONE"
  })
  void aSearchWithNoPlayerBehindItMatchesLiterally(String raw, MatchStrength expected) {
    final ItemFilter filter = ItemFilter.ofSearch(raw).withFuzzy(FuzzySearchAttachment.get(null));

    assertEquals(expected, FilterEvaluator.strengthOf(PICKAXE, filter));
  }
}
