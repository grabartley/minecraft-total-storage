package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TagSetFileNameTest {
  @ParameterizedTest
  @ValueSource(strings = {"base", "second-base", "my_tags", "set2"})
  void anOrdinaryNameIsTakenAsWritten(String name) {
    assertEquals(name, TagSetFileName.of(name).orElseThrow());
  }

  @Test
  void aNameIsFiledInLowerCaseSoTwoSpellingsAreOneFile() {
    assertEquals("base", TagSetFileName.of("BASE").orElseThrow());
  }

  @ParameterizedTest
  @ValueSource(strings = {"../escape", "a/b", "a:b", "a.json", "with space", "nul*"})
  void aNameThatCouldReachOutsideTheTagSetFolderIsRefused(String name) {
    assertTrue(TagSetFileName.of(name).isEmpty());
  }

  @Test
  void aNameCarryingABackslashIsRefused() {
    assertTrue(TagSetFileName.of("a" + ((char) 92) + "b").isEmpty());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void anAbsentNameIsRefused(String name) {
    assertTrue(TagSetFileName.of(name).isEmpty());
  }

  @Test
  void aNameLongerThanTheLimitIsRefused() {
    assertTrue(TagSetFileName.of("a".repeat(TagSetFileName.MAX_LENGTH + 1)).isEmpty());
  }
}
