package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player's request to flip the lock on one of their own inventory slots.
 *
 * <p>The payload carries an index and nothing else. It names no player, because the server already
 * knows who sent it, and it names no desired state, because a toggle the client computed could be
 * built from a stale mirror and would fight the server's set rather than follow it.
 */
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
