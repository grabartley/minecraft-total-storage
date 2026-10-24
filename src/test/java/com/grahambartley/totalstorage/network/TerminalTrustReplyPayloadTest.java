package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.access.TrustEditOutcome;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalTrustReplyPayloadTest {
  @ParameterizedTest
  @EnumSource(TrustEditOutcome.class)
  void everyOutcomeSurvivesTheWireUnchanged(TrustEditOutcome outcome) {
    TerminalTrustReplyPayload sent = new TerminalTrustReplyPayload(9, "Alex", outcome);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    TerminalTrustReplyPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, TerminalTrustReplyPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aReplyWithNoOutcomeIsRejected() {
    assertThrows(NullPointerException.class, () -> new TerminalTrustReplyPayload(1, "Alex", null));
  }
}
