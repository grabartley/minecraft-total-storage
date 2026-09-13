package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagSetImportModeTest {
  @ParameterizedTest
  @EnumSource(TagSetImportMode.class)
  void everyModeAnswersToItsOwnId(TagSetImportMode mode) {
    assertEquals(mode, TagSetImportMode.fromId(mode.id()).orElseThrow());
  }

  @ParameterizedTest
  @ValueSource(strings = {"MERGE", " merge ", "Replace"})
  void aModeIsRecognisedHoweverItWasTyped(String typed) {
    assertTrue(TagSetImportMode.fromId(typed).isPresent());
  }

  @Test
  void anythingElseNamesNoMode() {
    assertTrue(TagSetImportMode.fromId("overwrite").isEmpty());
    assertTrue(TagSetImportMode.fromId(null).isEmpty());
  }

  @Test
  void onlyReplacingClearsWhatTheNetworkAlreadyHad() {
    assertTrue(TagSetImportMode.REPLACE.clearsWhatIsThere());
    assertFalse(TagSetImportMode.MERGE.clearsWhatIsThere());
  }
}
