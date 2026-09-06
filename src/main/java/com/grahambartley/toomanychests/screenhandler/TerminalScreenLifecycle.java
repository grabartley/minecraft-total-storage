package com.grahambartley.toomanychests.screenhandler;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Closes a terminal screen its player did not walk away from.
 *
 * <p>Dying leaves a screen handler behind. Vanilla neither closes it nor keeps ticking the entity
 * that holds it, so the recurring {@link TerminalScreenHandler#canUse} check never runs again and
 * the viewer entry outlives the player it was written for. Every later resync and every access
 * revocation would then be aimed at somebody who has respawned somewhere else entirely.
 *
 * <p>Only this mod's own screens are closed. What another mod does with a screen its player died in
 * is that mod's decision.
 */
public final class TerminalScreenLifecycle {

  private TerminalScreenLifecycle() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, damageSource) -> {
          if (entity instanceof ServerPlayerEntity player
              && player.currentScreenHandler instanceof TerminalScreenHandler) {
            player.closeHandledScreen();
          }
        });
  }
}
