package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.access.PlayerNameRule;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalTrustEditPayloadTest {
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void anEditSurvivesTheWireUnchanged(boolean trusted) {
    TerminalTrustEditPayload sent = new TerminalTrustEditPayload(4, "Steve", trusted);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    TerminalTrustEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, TerminalTrustEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void anOverlongNameIsCutToWhatTheWireCarries() {
    TerminalTrustEditPayload sent = new TerminalTrustEditPayload(1, "x".repeat(200), true);

    assertEquals(PlayerNameRule.MAX_SHOWN_LENGTH, sent.player().length());
  }

  @Test
  void aMissingNameIsSentAsAnEmptyOne() {
    assertEquals("", new TerminalTrustEditPayload(1, null, true).player());
  }
}
