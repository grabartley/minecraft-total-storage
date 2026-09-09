package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ContainerRoleEviction {
  private final ServerWorld world;
  private final BlockPos terminal;

  public ContainerRoleEviction(ServerWorld world, BlockPos terminal) {
    this.world = Objects.requireNonNull(world, "world");
    this.terminal = Objects.requireNonNull(terminal, "terminal").toImmutable();
  }

  public boolean containerRemoved(StorageOrigin origin) {
    if (!(origin instanceof BlockStorageOrigin block)
        || !world.getRegistryKey().equals(block.worldKey())) {
      return false;
    }
    return forget(block.pos());
  }

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
