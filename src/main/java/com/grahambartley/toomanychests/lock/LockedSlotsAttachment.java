package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.TooManyChests;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

/**
 * Binds a player's locked slots to the player, so the set saves with the world, follows them
 * through respawn, and reaches only their own client.
 *
 * <p>The locks survive death while the items themselves drop per vanilla rules: a lock preserves
 * layout, it is not a keep-inventory mechanic.
 */
public final class LockedSlotsAttachment {
  public static final Identifier ID = Identifier.of(TooManyChests.MOD_ID, "locked_slots");

  public static final PacketCodec<RegistryByteBuf, LockedSlots> PACKET_CODEC =
      PacketCodecs.VAR_INT
          .collect(PacketCodecs.toList())
          .xmap(LockedSlots::of, LockedSlots::sortedIndices)
          .cast();

  public static final AttachmentSyncPredicate SYNC_PREDICATE = AttachmentSyncPredicate.targetOnly();

  public static final AttachmentType<LockedSlots> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(LockedSlotsCodec.CODEC)
                  .copyOnDeath()
                  .syncWith(PACKET_CODEC, SYNC_PREDICATE));

  private LockedSlotsAttachment() {}

  /**
   * Forces this class to initialise, which is what performs the registration. Without an explicit
   * call during mod init, the first read of {@link #TYPE} could land after a save has already been
   * parsed, and the stored locks would be discarded as an unknown attachment.
   */
  public static void register() {}

  public static LockedSlots get(PlayerEntity player) {
    return player == null
        ? LockedSlots.empty()
        : player.getAttachedOrElse(TYPE, LockedSlots.empty());
  }

  public static void set(PlayerEntity player, LockedSlots locked) {
    player.setAttached(TYPE, locked == null ? LockedSlots.empty() : locked);
  }

  public static LockedSlots toggle(PlayerEntity player, int slot) {
    final LockedSlots toggled = get(player).toggled(slot);
    set(player, toggled);
    return toggled;
  }
}
