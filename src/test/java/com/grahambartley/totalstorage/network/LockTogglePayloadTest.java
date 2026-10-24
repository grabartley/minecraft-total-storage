package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LockTogglePayloadTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 8, 9, 35, 36, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
  void anySlotIndexSurvivesTheWireUnchanged(int slot) {
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    LockTogglePayload.CODEC.encode(buffer, new LockTogglePayload(slot));

    assertEquals(slot, LockTogglePayload.CODEC.decode(buffer).slot());
  }

  @Test
  void theWholePayloadIsOneIndexAndNothingElse() {
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    LockTogglePayload.CODEC.encode(buffer, new LockTogglePayload(35));
    LockTogglePayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void everyPayloadReportsTheSharedChannelId() {
    assertSame(LockTogglePayload.ID, new LockTogglePayload(3).getId());
    assertEquals("total-storage:lock_toggle", LockTogglePayload.ID.id().toString());
  }
}
