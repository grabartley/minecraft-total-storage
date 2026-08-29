package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.ScanRegion;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

/**
 * Turns a terminal's redstone signal into the life and death of its network.
 *
 * <p>Per ADR 0007 the two states are total, so this is the only place a network is built or torn
 * down. Powering on costs one discovery scan and powering off costs one teardown, and nothing here
 * runs on a tick.
 *
 * <p>Every entry point is idempotent against the state already recorded, which is what keeps a
 * terminal surrounded by redstone from rescanning once per neighbour update.
 */
public final class TerminalPower {
  private final TerminalNetworkRegistry<NetworkMembership> registry;

  public TerminalPower(TerminalNetworkRegistry<NetworkMembership> registry) {
    this.registry = Objects.requireNonNull(registry, "registry");
  }

  public TerminalNetworkRegistry<NetworkMembership> registry() {
    return registry;
  }

  public Optional<NetworkMembership> network(ServerWorld world, BlockPos pos) {
    return world == null || pos == null
        ? Optional.empty()
        : registry.find(TerminalAddress.of(world, pos));
  }

  public boolean isActive(ServerWorld world, BlockPos pos) {
    return network(world, pos).isPresent();
  }

  /**
   * Answers a neighbour update. The block state is the record of what the terminal is currently
   * doing, so a signal that agrees with it means nothing has happened and no scan is owed.
   */
  public void signalChanged(ServerWorld world, BlockPos pos, BlockState state) {
    if (world == null || pos == null || state == null) {
      return;
    }
    final boolean receiving = world.isReceivingRedstonePower(pos);
    if (receiving == state.get(TerminalBlock.POWERED)) {
      return;
    }
    world.setBlockState(pos, state.with(TerminalBlock.POWERED, receiving), Block.NOTIFY_ALL);
    if (receiving) {
      activate(world, pos);
    } else {
      deactivate(world, pos);
    }
    world.playSound(
        null,
        pos,
        receiving ? ModSounds.TERMINAL_POWER_ON : ModSounds.TERMINAL_POWER_OFF,
        SoundCategory.BLOCKS,
        0.6f,
        1.0f);
  }

  /**
   * Brings a terminal's network in line with the block state it loaded with, so a terminal that was
   * powered when its chunk was saved is running again the moment the chunk is back. The network
   * itself is memory only: nothing about it is written to disk, and nothing needs to be.
   */
  public void loaded(ServerWorld world, BlockPos pos, BlockState state) {
    if (world == null || pos == null || state == null || !state.contains(TerminalBlock.POWERED)) {
      return;
    }
    if (state.get(TerminalBlock.POWERED)) {
      activate(world, pos);
    } else {
      deactivate(world, pos);
    }
  }

  /** A terminal that has been broken or whose chunk has gone leaves nothing listening behind it. */
  public void unloaded(ServerWorld world, BlockPos pos) {
    deactivate(world, pos);
  }

  /** Shuts down every network at once, which only a server stopping asks for. */
  public void shutDownAll() {
    registry.clear().forEach(NetworkMembership::powerOff);
  }

  private void activate(ServerWorld world, BlockPos pos) {
    final TerminalAddress address = TerminalAddress.of(world, pos);
    if (registry.isActive(address)) {
      return;
    }
    final NetworkMembership network =
        NetworkMembership.around(world, new ScanRegion(pos, radiusOf(world, pos)));
    network.powerOn();
    registry.activate(address, network).ifPresent(NetworkMembership::powerOff);
  }

  private void deactivate(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return;
    }
    registry.deactivate(TerminalAddress.of(world, pos)).ifPresent(NetworkMembership::powerOff);
  }

  private static int radiusOf(ServerWorld world, BlockPos pos) {
    return world.getBlockEntity(pos) instanceof TerminalBlockEntity terminal
        ? terminal.settings().radius()
        : TerminalSettings.DEFAULT_RADIUS;
  }
}
