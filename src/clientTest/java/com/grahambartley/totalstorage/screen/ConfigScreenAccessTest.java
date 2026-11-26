package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.screen.ConfigScreenAccess.Notice;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigScreenAccessTest {
  @ParameterizedTest
  @CsvSource({
    "false, false, false, false",
    "false, true, false, false",
    "true, false, true, false",
    "true, true, true, true"
  })
  void serverSettingsShowOnceReceivedAndAreLiveOnlyForAnEditor(
      boolean received, boolean editable, boolean shown, boolean active) {
    final ConfigScreenAccess access = new ConfigScreenAccess(received, editable);

    assertEquals(shown, access.showsServerSettings());
    assertEquals(active, access.serverControlsActive());
  }

  @ParameterizedTest
  @CsvSource({
    "false, false, false, false",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "true, true, true, true"
  })
  void doneSendsServerEditsOnlyWhenAnEditorChangedSomething(
      boolean received, boolean editable, boolean dirty, boolean sends) {
    assertEquals(sends, new ConfigScreenAccess(received, editable).sendsServerEdits(dirty));
  }

  @ParameterizedTest
  @CsvSource({
    "false, false, false, NOT_CONNECTED",
    "false, true, false, NOT_CONNECTED",
    "true, false, false, READ_ONLY",
    "true, true, false, NONE",
    "false, false, true, SAVE_FAILED",
    "true, false, true, SAVE_FAILED",
    "true, true, true, SAVE_FAILED"
  })
  void theHeaderSaysWhyTheScreenLooksTheWayItDoes(
      boolean received, boolean editable, boolean saveFailed, Notice expected) {
    assertEquals(expected, new ConfigScreenAccess(received, editable).notice(saveFailed));
  }
}
