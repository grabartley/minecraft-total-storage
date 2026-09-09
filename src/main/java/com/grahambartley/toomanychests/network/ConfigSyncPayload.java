package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.ConfigPacketCodec;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigSyncPayload(TooManyChestsConfig config) implements CustomPayload {
  public static final CustomPayload.Id<ConfigSyncPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "config_sync"));

  public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC =
      ConfigPacketCodec.INSTANCE.xmap(ConfigSyncPayload::new, ConfigSyncPayload::config);

  @Override
  public CustomPayload.Id<ConfigSyncPayload> getId() {
    return ID;
  }
}
