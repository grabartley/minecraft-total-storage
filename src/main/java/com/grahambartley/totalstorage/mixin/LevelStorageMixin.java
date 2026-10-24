package com.grahambartley.totalstorage.mixin;

import com.grahambartley.totalstorage.config.ServerConfigService;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {
  @Inject(method = "createSession", at = @At("RETURN"))
  private void totalStorage$loadServerConfig(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }

  @Inject(method = "createSessionWithoutSymlinkCheck", at = @At("RETURN"))
  private void totalStorage$loadServerConfigWithoutSymlinkCheck(
      String directoryName, CallbackInfoReturnable<LevelStorage.Session> cir) {
    ServerConfigService.loadFromSession(cir.getReturnValue());
  }
}
