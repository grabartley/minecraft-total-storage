package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Forgets what a terminal decided about a container once that container has gone.
 *
 * <p>Assignments are keyed by position because a chest carries no identity that survives being
 * broken. Left alone, the next chest placed on that spot would inherit an {@code EXCLUDED} the
 * player granted to a different one, which is the trust ADR 0004 exists to protect being quietly
 * spent on a container nobody vouched for.
 *
 * <p>Only removal forgets. A chunk unloading takes its containers out of the network without
 * touching what the terminal decided about them, so a container that leaves and comes back is the
 * same container to its network; see ADR 0014 for why removal is the one event that can be trusted
 * to mean the container is really gone.
 *
 * <p>Scoped to one terminal, and driven by the network that terminal is running, so an unpowered
 * terminal hears nothing. Per ADR 0007 that is what unpowered means.
 */
public final class ContainerRoleEviction {
  private final ServerWorld world;
  private final BlockPos terminal;

  public ContainerRoleEviction(ServerWorld world, BlockPos terminal) {
    this.world = Objects.requireNonNull(world, "world");
    this.terminal = Objects.requireNonNull(terminal, "terminal").toImmutable();
  }

  /** Whether anything was actually forgotten, which is what a caller logs or ignores. */
  public boolean containerRemoved(StorageOrigin origin) {
    if (!(origin instanceof BlockStorageOrigin block)
        || !world.getRegistryKey().equals(block.worldKey())) {
      return false;
    }
    return forget(block.pos());
  }

  /**
   * A terminal whose chunk has gone is left alone rather than read. Reading it would ask the world
   * for a block entity, and asking is what loads a chunk back: a removal announced while chunks are
   * unloading would pull the terminal's chunk in again, which unloads again, which is a world that
   * never finishes unloading anything. Per ADR 0002 nothing here may force a chunk.
   *
   * <p>Nothing is lost by declining. A terminal in an unloaded chunk is running no network, so the
   * container being removed was not one it was holding a role for.
   */
  private boolean forget(BlockPos container) {
    if (!world.getChunkManager().isChunkLoaded(terminal.getX() >> 4, terminal.getZ() >> 4)) {
      return false;
    }
    final TerminalBlockEntity blockEntity = TerminalLookup.at(world, terminal).orElse(null);
    if (blockEntity == null) {
      return false;
    }
    final TerminalSettings settings = blockEntity.settings();
    final TerminalSettings forgotten = settings.forgetContainer(container);
    if (forgotten.equals(settings)) {
      return false;
    }
    blockEntity.setSettings(forgotten);
    return true;
  }
}
