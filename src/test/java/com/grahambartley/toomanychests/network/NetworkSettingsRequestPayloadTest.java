package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkSettingsRequestPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  @Test
  void whatThePanelAskedForSurvivesTheWireUnchanged() {
    NetworkSettingsRequestPayload sent = new NetworkSettingsRequestPayload(6, 12, 8);
    RegistryByteBuf buffer = buffer();

    NetworkSettingsRequestPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, NetworkSettingsRequestPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MAX_VALUE, 1000})
  void askingForMoreRowsThanAPageCarriesIsClamped(int length) {
    assertEquals(
        NetworkSettingsView.MAX_CONTAINERS,
        new NetworkSettingsRequestPayload(1, 0, length).length());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void anOffsetOrLengthBelowNothingBecomesNothing(int wrong) {
    NetworkSettingsRequestPayload payload = new NetworkSettingsRequestPayload(1, wrong, wrong);

    assertEquals(0, payload.offset());
    assertEquals(0, payload.length());
  }

  @Test
  void theWindowItAsksForIsTheOffsetAndLengthItCarries() {
    assertEquals(12, new NetworkSettingsRequestPayload(1, 12, 8).window().offset());
    assertEquals(8, new NetworkSettingsRequestPayload(1, 12, 8).window().length());
  }
}
