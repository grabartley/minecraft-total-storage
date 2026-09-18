package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagNameRuleTest {
  private static NetworkTagSet withCustom(String... ids) {
    NetworkTagSet tags = NetworkTagSet.defaults();
    for (String id : ids) {
      tags = tags.withCustomTag(id);
    }
    return tags;
  }

  @ParameterizedTest
  @ValueSource(strings = {"base build", "redstone-project", "gifts_2", "castle"})
  void anOrdinaryNameIsAccepted(String name) {
    assertEquals(TagNameOutcome.OK, TagNameRule.check(name, NetworkTagSet.defaults()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  void aNameThatIsOnlyWhitespaceIsBlank(String name) {
    assertEquals(TagNameOutcome.BLANK, TagNameRule.check(name, NetworkTagSet.defaults()));
  }

  @Test
  void aNameLongerThanTheLimitIsRefused() {
    final String name = "a".repeat(TagNameRule.MAX_LENGTH + 1);
    assertEquals(TagNameOutcome.TOO_LONG, TagNameRule.check(name, NetworkTagSet.defaults()));
  }

  @Test
  void aNameExactlyAtTheLimitIsAccepted() {
    final String name = "a".repeat(TagNameRule.MAX_LENGTH);
    assertEquals(TagNameOutcome.OK, TagNameRule.check(name, NetworkTagSet.defaults()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"drop/table", "colon:name", "star*", "hash#tag", "at@mod"})
  void aNameCarryingPunctuationTheQueryLanguageUsesIsRefused(String name) {
    assertEquals(
        TagNameOutcome.ILLEGAL_CHARACTER, TagNameRule.check(name, NetworkTagSet.defaults()));
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void aNameCollidingWithAPresetIsRefused(PresetTag preset) {
    assertEquals(
        TagNameOutcome.PRESET_COLLISION, TagNameRule.check(preset.id(), NetworkTagSet.defaults()));
  }

  @ParameterizedTest
  @CsvSource({"castle,castle", "castle,CASTLE", "castle,  Castle  "})
  void aNameAlreadyOnTheNetworkIsRefusedWhateverItsCase(String existing, String asked) {
    assertEquals(TagNameOutcome.DUPLICATE, TagNameRule.check(asked, withCustom(existing)));
  }

  @Test
  void renamingATagToTheNameItAlreadyHasIsNotADuplicate() {
    assertEquals(TagNameOutcome.OK, TagNameRule.check("Castle", withCustom("castle"), "castle"));
  }

  @Test
  void renamingOntoAnotherTagsNameIsStillADuplicate() {
    assertEquals(
        TagNameOutcome.DUPLICATE,
        TagNameRule.check("keep", withCustom("castle", "keep"), "castle"));
  }

  @ParameterizedTest
  @CsvSource({
    "Castle,castle",
    "'  Base Build  ',base-build",
    "REDSTONE-project,redstone-project",
    "'Base   Kit',base-kit",
    "'a b c',a-b-c"
  })
  void theIdIsTheNameNormalisedIntoOneSearchableToken(String name, String expected) {
    assertEquals(expected, TagNameRule.idFor(name));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a\tb", "a\nb", "a\u000bb"})
  void everyKindOfWhitespaceBecomesTheSameSeparator(String name) {
    assertEquals("a-b", TagNameRule.idFor(name));
  }

  @Test
  void anIdWithNoWhitespaceIsHandedBackUntouched() {
    assertEquals("base-kit", TagNameRule.idFor("Base-Kit"));
    assertEquals("minecraft:stone", TagNameRule.idFor("minecraft:stone"));
  }

  @Test
  void anIdNeverCarriesASpaceBecauseTheSearchBoxSplitsOnOne() {
    assertFalse(TagNameRule.idFor("Base Kit").contains(" "));
    assertEquals(TagNameOutcome.OK, TagNameRule.check("Base Kit", NetworkTagSet.defaults()));
  }

  @Test
  void onlyOkCountsAsAccepted() {
    assertTrue(TagNameOutcome.OK.isAccepted());
    for (TagNameOutcome outcome : TagNameOutcome.values()) {
      if (outcome != TagNameOutcome.OK) {
        assertFalse(outcome.isAccepted(), outcome + " must not be accepted");
      }
    }
  }
}
