package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerFilterEditPayloadTest {
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whatAContainerAcceptsReachesTheServerEitherWay(boolean accepted) {
    ContainerFilterEditPayload sent =
        new ContainerFilterEditPayload(4, new BlockPos(9, -60, 3), "ores", accepted);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    ContainerFilterEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, ContainerFilterEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aTagNameIsCanonicalisedIntoAnIdBeforeItIsSent() {
    assertEquals(
        "base-build",
        new ContainerFilterEditPayload(1, BlockPos.ORIGIN, "  Base Build ", true).tagId());
  }

  @Test
  void aRequestNamingNoContainerIsRefusedRatherThanSent() {
    assertThrows(
        NullPointerException.class, () -> new ContainerFilterEditPayload(1, null, "ores", true));
  }
}
