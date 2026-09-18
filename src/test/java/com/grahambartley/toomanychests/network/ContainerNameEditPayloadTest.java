package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.toomanychests.storage.ContainerNameRule;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerNameEditPayloadTest {
  @ParameterizedTest
  @ValueSource(strings = {"Potions", "Ores and ingots", ""})
  void aNameReachesTheContainerItRenames(String name) {
    ContainerNameEditPayload sent =
        new ContainerNameEditPayload(3, new BlockPos(-40, 12, 300), name);
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    ContainerNameEditPayload.CODEC.encode(buffer, sent);

    assertEquals(sent, ContainerNameEditPayload.CODEC.decode(buffer));
    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aNameIsSanitisedBeforeItIsEverSent() {
    assertEquals(
        "Ores and ingots",
        new ContainerNameEditPayload(1, BlockPos.ORIGIN, "  Ores   and\ningots ").name());
  }

  @Test
  void anOverlongNameIsCutDownRatherThanRefusedByTheBuffer() {
    ContainerNameEditPayload sent =
        new ContainerNameEditPayload(1, BlockPos.ORIGIN, "x".repeat(200));
    RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), null);

    ContainerNameEditPayload.CODEC.encode(buffer, sent);

    assertEquals(ContainerNameRule.MAX_LENGTH, sent.name().length());
    assertEquals(sent, ContainerNameEditPayload.CODEC.decode(buffer));
  }

  @Test
  void aRequestNamingNoContainerIsRefusedRatherThanSent() {
    assertThrows(
        NullPointerException.class, () -> new ContainerNameEditPayload(1, null, "Potions"));
  }
}
