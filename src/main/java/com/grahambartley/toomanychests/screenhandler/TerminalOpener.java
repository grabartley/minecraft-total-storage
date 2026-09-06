package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.OptionalInt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

/**
 * Puts the terminal screen in front of a player, or tells them why it will not open.
 *
 * <p>A viewer is recorded only once the screen really opened. Vanilla refuses to open a screen for
 * a player who is already in one or who is spectating, and an entry written before that refusal
 * would name a viewer who is looking at nothing, which is exactly the entry a revoke or a resync
 * would then act on.
 */
public final class TerminalOpener {

  private TerminalOpener() {}

  public static TerminalOpenState open(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
    final TerminalOpenState state = TerminalOpenGuard.checkForOpening(world, pos, player);
    if (!state.allowed()) {
      report(player, state);
      return state;
    }
    final OptionalInt syncId = player.openHandledScreen(new TerminalScreenFactory(world, pos));
    if (syncId.isEmpty()) {
      return TerminalOpenState.REFUSED;
    }
    TerminalAccessControl.viewers().opened(TerminalAddress.of(world, pos), player.getUuid());
    world.playSound(null, pos, ModSounds.TERMINAL_SCREEN_OPEN, SoundCategory.BLOCKS, 0.5f, 1.0f);
    return state;
  }

  private static void report(PlayerEntity player, TerminalOpenState state) {
    state.refusal().ifPresent(message -> player.sendMessage(message, true));
  }
}
