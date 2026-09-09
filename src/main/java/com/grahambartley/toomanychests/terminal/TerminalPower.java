package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.ScanRegion;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

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

  public void unloaded(ServerWorld world, BlockPos pos) {
    deactivate(world, pos);
  }

  public boolean rescan(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return false;
    }
    final NetworkMembership running = registry.find(TerminalAddress.of(world, pos)).orElse(null);
    if (running == null || running.region().equals(scanRegion(world, pos))) {
      return false;
    }
    deactivate(world, pos);
    activate(world, pos);
    return true;
  }

  public void shutDownAll() {
    registry.clear().forEach(NetworkMembership::powerOff);
  }

  private void activate(ServerWorld world, BlockPos pos) {
    final TerminalAddress address = TerminalAddress.of(world, pos);
    if (registry.isActive(address)) {
      return;
    }
    final NetworkMembership network = NetworkMembership.around(world, scanRegion(world, pos));
    network.powerOn();
    registry.activate(address, network).ifPresent(NetworkMembership::powerOff);
  }

  private void deactivate(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return;
    }
    registry.deactivate(TerminalAddress.of(world, pos)).ifPresent(NetworkMembership::powerOff);
  }

  private static ScanRegion scanRegion(ServerWorld world, BlockPos pos) {
    final int configured =
        world.getBlockEntity(pos) instanceof TerminalBlockEntity terminal
            ? terminal.settings().radius()
            : TerminalSettings.DEFAULT_RADIUS;
    return new ScanRegion(
        pos,
        TerminalRadius.effective(configured, ServerConfigService.current().maxTerminalRadius()));
  }
}
