package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;

class TagPanelRequestPayloadTest {
  private static TagPanelRequestPayload roundTrip(TagPanelRequestPayload payload) {
    final RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);
    TagPanelRequestPayload.CODEC.encode(buffer, payload);
    return TagPanelRequestPayload.CODEC.decode(buffer);
  }

  @Test
  void openingWithoutASubjectSurvivesTheWire() {
    final TagPanelRequestPayload sent = TagPanelRequestPayload.opened(4);

    assertEquals(sent, roundTrip(sent));
    assertTrue(sent.open());
    assertEquals(Optional.empty(), sent.subject());
  }

  @Test
  void closingSurvivesTheWireAndNamesNoSubject() {
    final TagPanelRequestPayload sent = TagPanelRequestPayload.closed(4);

    assertEquals(sent, roundTrip(sent));
    assertFalse(sent.open());
    assertEquals(Optional.empty(), sent.subject());
  }

  @Test
  void askingForAnItemWithNoVariantOpensTheWholePanel() {
    final TagPanelRequestPayload payload = TagPanelRequestPayload.forItem(4, null);

    assertTrue(payload.open());
    assertEquals(Optional.empty(), payload.subject());
  }

  @Test
  void aMissingSubjectArrivesEmptyRatherThanNull() {
    assertEquals(Optional.empty(), new TagPanelRequestPayload(4, true, null).subject());
  }
}
