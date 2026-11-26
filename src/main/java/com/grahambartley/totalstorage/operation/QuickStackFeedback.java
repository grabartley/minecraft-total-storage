package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

public final class QuickStackFeedback {
  private QuickStackFeedback() {}

  public static void report(ServerPlayerEntity player, QuickStackOutcome outcome) {
    if (player == null || outcome == null || !outcome.result().movedAnything()) {
      return;
    }
    player
        .getWorld()
        .playSound(
            null,
            player.getBlockPos(),
            ModSounds.TERMINAL_DEPOSIT_CONFIRM,
            SoundCategory.BLOCKS,
            0.5f,
            1.0f);
  }
}
