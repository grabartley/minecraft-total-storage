package com.grahambartley.toomanychests.mixin;

import com.grahambartley.toomanychests.mutation.ContainerMutations;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
  @Shadow @Final protected BlockPos pos;

  @Shadow protected World world;

  @Inject(method = "markDirty()V", at = @At("TAIL"))
  private void tooManyChests$publishContainerMutation(CallbackInfo ci) {
    ContainerMutations.publish(world, pos);
  }
}
