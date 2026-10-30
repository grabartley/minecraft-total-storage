package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContainerLocatePayloadTest {
  @ParameterizedTest(name = "screen {0} at {1} {2} {3}")
  @CsvSource({"3, 4, -60, 12", "0, 0, 0, 0", "127, -3000, 319, 3000"})
  void aRequestSurvivesTheWire(int syncId, int x, int y, int z) {
    final ContainerLocatePayload sent = new ContainerLocatePayload(syncId, new BlockPos(x, y, z));
    final RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    ContainerLocatePayload.CODEC.encode(buffer, sent);

    assertEquals(sent, ContainerLocatePayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aRequestMustNameAContainer() {
    assertThrows(NullPointerException.class, () -> new ContainerLocatePayload(1, null));
  }
}
