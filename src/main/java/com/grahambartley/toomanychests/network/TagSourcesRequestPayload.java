package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TagSourcesRequestPayload(int syncId) implements CustomPayload {
  public static final CustomPayload.Id<TagSourcesRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "tag_sources_request"));

  public static final PacketCodec<RegistryByteBuf, TagSourcesRequestPayload> CODEC =
      PacketCodecs.VAR_INT
          .xmap(TagSourcesRequestPayload::new, TagSourcesRequestPayload::syncId)
          .cast();

  @Override
  public CustomPayload.Id<TagSourcesRequestPayload> getId() {
    return ID;
  }
}
