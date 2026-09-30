package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientModPresencePayload() implements CustomPayload {
  public static final CustomPayload.Id<ClientModPresencePayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "client_mod_presence"));

  public static final PacketCodec<PacketByteBuf, ClientModPresencePayload> CODEC =
      PacketCodec.unit(new ClientModPresencePayload());

  @Override
  public CustomPayload.Id<ClientModPresencePayload> getId() {
    return ID;
  }
}
