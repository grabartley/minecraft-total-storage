package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.terminal.TerminalSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalRadiusEditPayloadTest {
  @Test
  void theRadiusAskedForSurvivesTheWireUnchanged() {
    TerminalRadiusEditPayload sent = new TerminalRadiusEditPayload(3, 12);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    TerminalRadiusEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, TerminalRadiusEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MAX_VALUE, 10_000})
  void aRadiusAboveWhatOneCanBeIsClamped(int blocks) {
    assertEquals(TerminalSettings.MAX_RADIUS, new TerminalRadiusEditPayload(1, blocks).blocks());
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MIN_VALUE, -1, 0})
  void aRadiusBelowWhatOneCanBeIsClamped(int blocks) {
    assertEquals(TerminalSettings.MIN_RADIUS, new TerminalRadiusEditPayload(1, blocks).blocks());
  }
}
