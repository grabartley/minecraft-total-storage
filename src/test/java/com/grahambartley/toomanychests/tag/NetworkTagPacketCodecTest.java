package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Set;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NetworkTagPacketCodecTest {
  private static List<NetworkTagSet> wireCases() {
    return List.of(
        NetworkTagSet.defaults(),
        NetworkTagSet.defaults().withCustomTag("ores", "Ores"),
        NetworkTagSet.defaults()
            .withCustomTag("ores", "Ores")
            .withCustomTag("crops", "Crops")
            .withItem("ores", "minecraft:coal")
            .withItem("ores", "minecraft:raw_iron")
            .withItem("crops", "minecraft:wheat"));
  }

  @ParameterizedTest
  @MethodSource("wireCases")
  void aTagSetSurvivesTheWireUnchanged(NetworkTagSet tags) {
    RegistryByteBuf buffer = buffer();

    NetworkTagPacketCodec.INSTANCE.encode(buffer, tags);

    assertEquals(tags, NetworkTagPacketCodec.INSTANCE.decode(buffer));
  }

  @ParameterizedTest
  @MethodSource("wireCases")
  void decodingConsumesExactlyWhatEncodingWrote(NetworkTagSet tags) {
    RegistryByteBuf buffer = buffer();

    NetworkTagPacketCodec.INSTANCE.encode(buffer, tags);
    NetworkTagPacketCodec.INSTANCE.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void aMultiTaggedItemArrivesCarryingEveryTag() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("ores")
            .withCustomTag("smeltable")
            .withItem("ores", "minecraft:raw_iron")
            .withItem("smeltable", "minecraft:raw_iron");
    RegistryByteBuf buffer = buffer();

    NetworkTagPacketCodec.INSTANCE.encode(buffer, tags);

    assertEquals(
        Set.of("ores", "smeltable"),
        NetworkTagPacketCodec.INSTANCE.decode(buffer).tagIdsFor("minecraft:raw_iron"));
  }

  @Test
  void presetsDoNotTravelBecauseTheClientAlreadyHasThem() {
    RegistryByteBuf empty = buffer();
    RegistryByteBuf oneCustom = buffer();

    NetworkTagPacketCodec.INSTANCE.encode(empty, NetworkTagSet.defaults());
    NetworkTagPacketCodec.INSTANCE.encode(
        oneCustom, NetworkTagSet.defaults().withCustomTag("ores"));

    assertEquals(1, empty.readableBytes());
    assertTrue(oneCustom.readableBytes() > empty.readableBytes());
    assertEquals(
        NetworkTagSet.defaults().all(), NetworkTagPacketCodec.INSTANCE.decode(empty).all());
  }

  @Test
  void aTagWithABlankIdOnTheWireIsDroppedRatherThanRefusingThePacket() {
    RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(2);
    writeRawTag(buffer, "   ");
    writeRawTag(buffer, "ores");

    assertEquals(
        List.of(NetworkTag.custom("ores")), NetworkTagPacketCodec.INSTANCE.decode(buffer).custom());
  }

  private static void writeRawTag(PacketByteBuf buffer, String id) {
    buffer.writeString(id);
    buffer.writeString(id);
    buffer.writeCollection(List.<String>of(), PacketByteBuf::writeString);
  }

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }
}
