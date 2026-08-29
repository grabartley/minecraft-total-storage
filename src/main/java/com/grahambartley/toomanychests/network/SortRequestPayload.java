package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player's request to sort the screen they have open.
 *
 * <p>Per ADR 0009 this carries intent and nothing else. The sync id names which screen, and the
 * server checks it against the handler it already holds for that player rather than trusting it, so
 * the packet cannot reach a container the sender does not have open. It names no slots and no
 * ordering, because a client that could supply either could rearrange a chest into any shape it
 * liked.
 */
public record SortRequestPayload(int syncId) implements CustomPayload {
  public static final CustomPayload.Id<SortRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "sort_request"));

  public static final PacketCodec<RegistryByteBuf, SortRequestPayload> CODEC =
      PacketCodecs.VAR_INT.xmap(SortRequestPayload::new, SortRequestPayload::syncId).cast();

  @Override
  public CustomPayload.Id<SortRequestPayload> getId() {
    return ID;
  }
}
