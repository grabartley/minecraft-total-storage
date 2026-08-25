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

/**
 * The single place a container announces that its contents changed.
 *
 * <p>{@code Inventory#markDirty} resolves here for every vanilla container, and hoppers, droppers,
 * screen handlers and the Transfer API all call it on every content change, so hooking it replaces
 * every form of polling.
 *
 * <p>Not filtered to {@code Inventory}: a modded block entity may back item storage without
 * implementing it, and the registry already ignores any position no terminal asked about, so the
 * filter would buy nothing and lose those containers.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

  @Shadow @Final protected BlockPos pos;

  @Shadow protected World world;

  @Inject(method = "markDirty()V", at = @At("TAIL"))
  private void tooManyChests$publishContainerMutation(CallbackInfo ci) {
    ContainerMutations.publish(world, pos);
  }
}
