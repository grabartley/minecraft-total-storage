package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SortRequestPayload(int syncId) implements CustomPayload {
  public static final CustomPayload.Id<SortRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "sort_request"));

  public static final PacketCodec<RegistryByteBuf, SortRequestPayload> CODEC =
      PacketCodecs.VAR_INT.xmap(SortRequestPayload::new, SortRequestPayload::syncId).cast();

  @Override
  public CustomPayload.Id<SortRequestPayload> getId() {
    return ID;
  }
}
