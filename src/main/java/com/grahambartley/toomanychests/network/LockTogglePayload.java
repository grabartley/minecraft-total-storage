package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LockTogglePayload(int slot) implements CustomPayload {
  public static final CustomPayload.Id<LockTogglePayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "lock_toggle"));

  public static final PacketCodec<RegistryByteBuf, LockTogglePayload> CODEC =
      PacketCodecs.VAR_INT.xmap(LockTogglePayload::new, LockTogglePayload::slot).cast();

  @Override
  public CustomPayload.Id<LockTogglePayload> getId() {
    return ID;
  }
}
