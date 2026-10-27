package com.grahambartley.totalstorage.filter;

import com.grahambartley.totalstorage.TotalStorage;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class FuzzySearchAttachment {
  public static final Identifier ID = Identifier.of(TotalStorage.MOD_ID, "fuzzy_search");

  public static final boolean DEFAULT = false;

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .copyOnDeath()
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.targetOnly()));

  private FuzzySearchAttachment() {}

  public static void register() {}

  public static boolean get(PlayerEntity player) {
    return player != null && player.getAttachedOrElse(TYPE, DEFAULT);
  }

  public static void set(PlayerEntity player, boolean enabled) {
    player.setAttached(TYPE, enabled);
  }

  public static ItemFilter applyTo(PlayerEntity player, ItemFilter filter) {
    return (filter == null ? ItemFilter.NONE : filter).withFuzzy(get(player));
  }
}
