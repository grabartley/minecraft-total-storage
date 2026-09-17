package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    NetworkSettingsRequestPayload sent = NetworkSettingsRequestPayload.showing(6, 12, 8);
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
        NetworkSettingsRequestPayload.showing(1, 0, length).length());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void anOffsetOrLengthBelowNothingBecomesNothing(int wrong) {
    NetworkSettingsRequestPayload payload = NetworkSettingsRequestPayload.showing(1, wrong, wrong);

    assertEquals(0, payload.offset());
    assertEquals(0, payload.length());
  }

  @Test
  void aPanelThatHasBeenHiddenSaysSoAndAsksForNothing() {
    NetworkSettingsRequestPayload closed = NetworkSettingsRequestPayload.closed(6);

    assertFalse(closed.showing());
    assertEquals(0, closed.offset());
    assertEquals(0, closed.length());
  }

  @Test
  void whetherThePanelIsShowingSurvivesTheWire() {
    RegistryByteBuf buffer = buffer();
    NetworkSettingsRequestPayload sent = NetworkSettingsRequestPayload.closed(6);

    NetworkSettingsRequestPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, NetworkSettingsRequestPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void theWindowItAsksForIsTheOffsetAndLengthItCarries() {
    assertEquals(12, NetworkSettingsRequestPayload.showing(1, 12, 8).window().offset());
    assertEquals(8, NetworkSettingsRequestPayload.showing(1, 12, 8).window().length());
  }
}
