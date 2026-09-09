package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DepositAllRequestPayload(int syncId) implements CustomPayload {
  public static final CustomPayload.Id<DepositAllRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "deposit_all_request"));

  public static final PacketCodec<RegistryByteBuf, DepositAllRequestPayload> CODEC =
      PacketCodecs.VAR_INT
          .xmap(DepositAllRequestPayload::new, DepositAllRequestPayload::syncId)
          .cast();

  @Override
  public CustomPayload.Id<DepositAllRequestPayload> getId() {
    return ID;
  }
}
