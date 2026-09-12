package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigPacketCodecTest {
  static Stream<TooManyChestsConfig> configs() {
    return Stream.of(
        TooManyChestsConfig.defaults(),
        TestConfigs.everythingNonDefault(),
        TestConfigs.withModes(TerminalAccessMode.TRUSTED, SortOrdering.DISPLAY_NAME),
        TestConfigs.withInts(1, 1, 0, 0, 1),
        TestConfigs.withInts(64, 64, 100, 200, 1024),
        TestConfigs.withOverrides(Map.of()),
        TestConfigs.withOverrides(Map.of("minecraft:chest", false)));
  }

  @ParameterizedTest
  @MethodSource("configs")
  void everySettingSurvivesTheWireUnchanged(TooManyChestsConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigPacketCodec.INSTANCE.encode(buffer, config);

    assertEquals(config, ConfigPacketCodec.INSTANCE.decode(buffer));
  }

  @ParameterizedTest
  @MethodSource("configs")
  void decodingConsumesExactlyWhatEncodingWrote(TooManyChestsConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigPacketCodec.INSTANCE.encode(buffer, config);
    ConfigPacketCodec.INSTANCE.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void anOutOfRangeValueOnTheWireIsClampedRatherThanTrusted() {
    RegistryByteBuf buffer = forged(9999, "owner", "registry_id");

    TooManyChestsConfig decoded = ConfigPacketCodec.INSTANCE.decode(buffer);

    assertEquals(TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX, decoded.maxTerminalRadius());
  }

  @Test
  void anUnrecognisedAccessModeFallsBackToTheDefault() {
    RegistryByteBuf buffer = forged(16, "everyone-including-me", "registry_id");

    TooManyChestsConfig decoded = ConfigPacketCodec.INSTANCE.decode(buffer);

    assertEquals(
        TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE, decoded.defaultTerminalAccessMode());
  }

  @Test
  void anUnrecognisedSortOrderingFallsBackToTheDefault() {
    RegistryByteBuf buffer = forged(16, "owner", "whatever-i-like");

    TooManyChestsConfig decoded = ConfigPacketCodec.INSTANCE.decode(buffer);

    assertEquals(TooManyChestsConfig.DEFAULT_SORT_ORDERING, decoded.sortOrdering());
  }

  @Test
  void anUnparseableOverrideKeyOnTheWireIsDropped() {
    RegistryByteBuf buffer = forged(16, "owner", "registry_id", Map.of("NOT AN ID", true));

    TooManyChestsConfig decoded = ConfigPacketCodec.INSTANCE.decode(buffer);

    assertEquals(Map.of(), decoded.containerEligibilityOverrides());
  }

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static RegistryByteBuf forged(int maxTerminalRadius, String accessMode, String ordering) {
    return forged(maxTerminalRadius, accessMode, ordering, Map.of());
  }

  private static RegistryByteBuf forged(
      int maxTerminalRadius, String accessMode, String ordering, Map<String, Boolean> overrides) {
    RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(maxTerminalRadius);
    buffer.writeString(accessMode);
    buffer.writeString(ordering);
    buffer.writeBoolean(true);
    buffer.writeBoolean(true);
    buffer.writeVarInt(8);
    buffer.writeBoolean(true);
    buffer.writeBoolean(false);
    buffer.writeVarInt(90);
    buffer.writeVarInt(10);
    buffer.writeVarInt(64);
    buffer.writeVarInt(8);
    buffer.writeMap(overrides, PacketByteBuf::writeString, PacketByteBuf::writeBoolean);
    return buffer;
  }
}
