package com.grahambartley.toomanychests.mixin;

import com.grahambartley.toomanychests.config.ServerConfigService;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Loads the world's config as soon as its save session opens, which is earlier than {@code
 * ServerLifecycleEvents.SERVER_STARTING}. Both session factory methods construct a session
 * directly, so both are hooked.
 */
@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {

  @Inject(method = "createSession", at = @At("RETURN"))
  private void tooManyChests$loadServerConfig(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }

  @Inject(method = "createSessionWithoutSymlinkCheck", at = @At("RETURN"))
  private void tooManyChests$loadServerConfigWithoutSymlinkCheck(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }
}
