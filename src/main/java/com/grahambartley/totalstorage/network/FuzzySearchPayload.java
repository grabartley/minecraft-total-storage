package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FuzzySearchPayload(boolean enabled) implements CustomPayload {
  public static final CustomPayload.Id<FuzzySearchPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "fuzzy_search"));

  public static final PacketCodec<RegistryByteBuf, FuzzySearchPayload> CODEC =
      PacketCodecs.BOOL.xmap(FuzzySearchPayload::new, FuzzySearchPayload::enabled).cast();

  @Override
  public CustomPayload.Id<FuzzySearchPayload> getId() {
    return ID;
  }
}
