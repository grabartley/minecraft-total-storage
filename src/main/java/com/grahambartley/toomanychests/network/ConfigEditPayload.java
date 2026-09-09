package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.ConfigPacketCodec;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigEditPayload(TooManyChestsConfig config) implements CustomPayload {
  public static final CustomPayload.Id<ConfigEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "config_edit"));

  public static final PacketCodec<RegistryByteBuf, ConfigEditPayload> CODEC =
      ConfigPacketCodec.INSTANCE.xmap(ConfigEditPayload::new, ConfigEditPayload::config);

  @Override
  public CustomPayload.Id<ConfigEditPayload> getId() {
    return ID;
  }
}
