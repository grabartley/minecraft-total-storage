package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.config.TestConfigs;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import io.netty.buffer.Unpooled;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigEditPayloadTest {
  static Stream<TotalStorageConfig> configs() {
    return Stream.of(TotalStorageConfig.defaults(), TestConfigs.everythingNonDefault());
  }

  @ParameterizedTest
  @MethodSource("configs")
  void theWholeRequestedConfigSurvivesTheWireUnchanged(TotalStorageConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigEditPayload.CODEC.encode(buffer, new ConfigEditPayload(config));

    assertEquals(config, ConfigEditPayload.CODEC.decode(buffer).config());
  }

  @ParameterizedTest
  @MethodSource("configs")
  void thePayloadIsOneConfigAndNothingElse(TotalStorageConfig config) {
    RegistryByteBuf buffer = buffer();

    ConfigEditPayload.CODEC.encode(buffer, new ConfigEditPayload(config));
    ConfigEditPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @Test
  void everyPayloadReportsTheSharedChannelId() {
    assertSame(ConfigEditPayload.ID, new ConfigEditPayload(TotalStorageConfig.defaults()).getId());
    assertEquals("total-storage:config_edit", ConfigEditPayload.ID.id().toString());
  }

  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }
}
