package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.ConfigSettings;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import org.junit.jupiter.api.Test;

class ServerConfigEditsTest {
  private static final ConfigSetting RADIUS =
      ConfigSettings.byName("maxTerminalRadius").orElseThrow();
  private static final ConfigSetting SORT_BUTTON =
      ConfigSettings.byName("sortButtonEnabled").orElseThrow();

  @Test
  void aScreenNobodyTouchedHasNothingToSubmit() {
    assertFalse(new ServerConfigEdits(TotalStorageConfig.defaults()).isDirty());
  }

  @Test
  void changingASettingIsSomethingToSubmit() {
    final ServerConfigEdits edits = new ServerConfigEdits(TotalStorageConfig.defaults());

    assertTrue(edits.set(RADIUS, "32"));

    assertTrue(edits.isDirty());
    assertEquals("32", edits.valueOf(RADIUS));
  }

  @Test
  void aValueOutsideItsRangeIsRefusedAndChangesNothing() {
    final ServerConfigEdits edits = new ServerConfigEdits(TotalStorageConfig.defaults());

    assertFalse(edits.set(RADIUS, "999"));

    assertFalse(edits.isDirty());
    assertEquals(RADIUS.defaultValue(), edits.valueOf(RADIUS));
  }

  @Test
  void aScreenNobodyTouchedFollowsTheServerWhenACommandMovesASetting() {
    final ServerConfigEdits edits = new ServerConfigEdits(TotalStorageConfig.defaults());
    final TotalStorageConfig moved =
        RADIUS.withValue(TotalStorageConfig.defaults(), "48").orElseThrow();

    assertTrue(edits.adopt(moved));

    assertEquals("48", edits.valueOf(RADIUS));
    assertFalse(edits.isDirty());
  }

  @Test
  void aScreenBeingEditedIsNeverOverwrittenUnderThePlayer() {
    final ServerConfigEdits edits = new ServerConfigEdits(TotalStorageConfig.defaults());
    edits.set(SORT_BUTTON, "false");

    assertFalse(edits.adopt(RADIUS.withValue(TotalStorageConfig.defaults(), "48").orElseThrow()));

    assertEquals("false", edits.valueOf(SORT_BUTTON));
    assertEquals(RADIUS.defaultValue(), edits.valueOf(RADIUS));
  }

  @Test
  void aServerSayingWhatTheScreenAlreadyShowsChangesNothing() {
    assertFalse(
        new ServerConfigEdits(TotalStorageConfig.defaults()).adopt(TotalStorageConfig.defaults()));
  }
}
