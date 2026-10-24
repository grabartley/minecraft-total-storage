package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.ConfigPacketCodec;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigEditPayload(TotalStorageConfig config) implements CustomPayload {
  public static final CustomPayload.Id<ConfigEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "config_edit"));

  public static final PacketCodec<RegistryByteBuf, ConfigEditPayload> CODEC =
      ConfigPacketCodec.INSTANCE.xmap(ConfigEditPayload::new, ConfigEditPayload::config);

  @Override
  public CustomPayload.Id<ConfigEditPayload> getId() {
    return ID;
  }
}
