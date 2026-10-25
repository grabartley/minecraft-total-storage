package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

class TerminalNoticePayloadTest {
  private static final int SYNC_ID = 4;

  @Test
  void aNoticeWithoutAHeadlineLeadsWithItsMessage() {
    final Text message = Text.literal("Nothing to sort");

    assertEquals(
        message,
        new TerminalNoticePayload(SYNC_ID, TerminalNoticeTone.PARTIAL, null, message).headline());
  }

  @Test
  void aNoticeMustSaySomethingAndHowItWent() {
    assertThrows(
        NullPointerException.class,
        () -> new TerminalNoticePayload(SYNC_ID, TerminalNoticeTone.FAILED, null, null));
    assertThrows(
        NullPointerException.class,
        () -> new TerminalNoticePayload(SYNC_ID, null, null, Text.literal("Sorted")));
  }

  @Test
  void aNoticeCarriesTheScreenAndToneItWasSentFor() {
    final TerminalNoticePayload payload =
        new TerminalNoticePayload(
            SYNC_ID, TerminalNoticeTone.FAILED, Text.literal("No room"), Text.literal("Full"));

    assertEquals(SYNC_ID, payload.syncId());
    assertEquals(TerminalNoticeTone.FAILED, payload.tone());
    assertEquals(TerminalNoticePayload.ID, payload.getId());
  }

  @Test
  void aNoticeIsNamedUnderTheModsOwnIdentifier() {
    assertTrue(TerminalNoticePayload.ID.id().toString().startsWith("total-storage:"));
  }
}
