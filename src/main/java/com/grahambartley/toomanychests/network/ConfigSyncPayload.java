package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.config.ConfigPacketCodec;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * The server telling a client what its configuration currently is.
 *
 * <p>The whole config travels rather than a delta, because a client that missed a packet would
 * otherwise apply the next change on top of the wrong base, and the settings are small enough that
 * resending all of them costs less than tracking which ones a client already has.
 */
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
