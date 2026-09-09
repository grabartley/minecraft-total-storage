package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalAccessEditPayloadTest {
  @ParameterizedTest
  @EnumSource(TerminalAccessMode.class)
  void everyModeSurvivesTheWireUnchanged(TerminalAccessMode mode) {
    TerminalAccessEditPayload sent = new TerminalAccessEditPayload(4, mode);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    TerminalAccessEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, TerminalAccessEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aRequestNamingNoModeDefaultsToTheMostClosedOne() {
    assertEquals(TerminalAccessMode.OWNER, new TerminalAccessEditPayload(1, null).mode());
  }
}
