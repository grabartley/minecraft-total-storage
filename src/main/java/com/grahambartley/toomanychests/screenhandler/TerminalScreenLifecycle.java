package com.grahambartley.toomanychests.screenhandler;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

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
