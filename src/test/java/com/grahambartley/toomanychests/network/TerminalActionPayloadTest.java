package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.terminal.TerminalAction;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalActionPayloadTest {

  @ParameterizedTest
  @EnumSource(TerminalAction.class)
  void everyActionSurvivesTheWireUnchanged(TerminalAction action) {
    TerminalActionPayload sent = new TerminalActionPayload(9, action);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    TerminalActionPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, TerminalActionPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  /** A payload that lost its action falls back to the one that changes nothing. */
  @Test
  void aRequestNamingNoActionDefaultsToTheHarmlessOne() {
    assertEquals(TerminalAction.HIGHLIGHT, new TerminalActionPayload(1, null).action());
  }
}
