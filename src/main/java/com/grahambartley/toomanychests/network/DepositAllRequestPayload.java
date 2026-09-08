package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player emptying their inventory into the network in one gesture.
 *
 * <p>It names a screen and nothing else. Which slots are eligible is the server's decision alone:
 * locked slots, armour and the offhand are excluded there, so a forged packet listing them gains
 * nothing because there is nowhere in this payload to list them.
 */
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
