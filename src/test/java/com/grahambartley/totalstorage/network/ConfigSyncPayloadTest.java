package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.config.TestConfigs;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import io.netty.buffer.Unpooled;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigSyncPayloadTest {
  static Stream<TotalStorageConfig> configs() {
    return Stream.of(TotalStorageConfig.defaults(), TestConfigs.everythingNonDefault());
  }

  @ParameterizedTest
  @MethodSource("configs")
  void theWholeConfigSurvivesTheWireUnchanged(TotalStorageConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigSyncPayload.CODEC.encode(buffer, new ConfigSyncPayload(config));

    assertEquals(config, ConfigSyncPayload.CODEC.decode(buffer).config());
  }

  @ParameterizedTest
  @MethodSource("configs")
  void thePayloadIsOneConfigAndNothingElse(TotalStorageConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigSyncPayload.CODEC.encode(buffer, new ConfigSyncPayload(config));
    ConfigSyncPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void everyPayloadReportsTheSharedChannelId() {
    assertSame(ConfigSyncPayload.ID, new ConfigSyncPayload(TotalStorageConfig.defaults()).getId());
    assertEquals("total-storage:config_sync", ConfigSyncPayload.ID.id().toString());
  }

  @Test
  void theSyncAndEditChannelsAreDistinct() {
    assertNotEquals(ConfigSyncPayload.ID.id(), ConfigEditPayload.ID.id());
  }

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }
}
