package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

class ClientModPresencePayloadTest {
  @Test
  void thePayloadCarriesNothingOnTheWire() {
    PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

    ClientModPresencePayload.CODEC.encode(buffer, new ClientModPresencePayload());

    assertEquals(0, buffer.readableBytes());
    assertEquals(new ClientModPresencePayload(), ClientModPresencePayload.CODEC.decode(buffer));
  }

  @Test
  void thePayloadIsNamedUnderTheMod() {
    assertEquals("too-many-chests", ClientModPresencePayload.ID.id().getNamespace());
  }
}
