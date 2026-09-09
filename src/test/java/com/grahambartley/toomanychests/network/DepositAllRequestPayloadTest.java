package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DepositAllRequestPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 99})
  void theScreenItNamesSurvivesTheWireUnchanged(int syncId) {
    RegistryByteBuf buffer = buffer();

    DepositAllRequestPayload.CODEC.encode(buffer, new DepositAllRequestPayload(syncId));

    assertEquals(syncId, DepositAllRequestPayload.CODEC.decode(buffer).syncId());
  }

  @Test
  void theWholePayloadIsReadBackWithNothingLeftOver() {
    RegistryByteBuf buffer = buffer();

    DepositAllRequestPayload.CODEC.encode(buffer, new DepositAllRequestPayload(4));
    DepositAllRequestPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }
}
