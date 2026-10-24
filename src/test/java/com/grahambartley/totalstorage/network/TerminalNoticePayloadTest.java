package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalNoticePayloadTest {
  private static final int SYNC_ID = 4;

  @Test
  void aNoticeCarriesTheScreenItBelongsTo() {
    TerminalNoticePayload payload =
        new TerminalNoticePayload(SYNC_ID, Text.literal("Sorted"), false);

    assertEquals(SYNC_ID, payload.syncId());
    assertEquals(TerminalNoticePayload.ID, payload.getId());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void aNoticeRemembersWhetherItIsAsking(boolean needsAnAnswer) {
    assertEquals(
        needsAnAnswer,
        new TerminalNoticePayload(SYNC_ID, Text.literal("Click again"), needsAnAnswer)
            .needsAnAnswer());
  }

  @Test
  void aNoticeIsNamedUnderTheModsOwnIdentifier() {
    assertTrue(TerminalNoticePayload.ID.id().toString().startsWith("total-storage:"));
  }
}
