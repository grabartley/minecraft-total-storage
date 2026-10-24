package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.ConfigPacketCodec;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigSyncPayload(TotalStorageConfig config) implements CustomPayload {
  public static final CustomPayload.Id<ConfigSyncPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "config_sync"));

  public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC =
      ConfigPacketCodec.INSTANCE.xmap(ConfigSyncPayload::new, ConfigSyncPayload::config);

  @Override
  public CustomPayload.Id<ConfigSyncPayload> getId() {
    return ID;
  }
}
