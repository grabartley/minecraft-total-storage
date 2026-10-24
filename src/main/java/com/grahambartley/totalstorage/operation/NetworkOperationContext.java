package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record NetworkOperationContext(ServerWorld world, BlockPos terminal, UUID actor) {
  public NetworkOperationContext {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(actor, "actor");
    terminal = terminal.toImmutable();
  }

  public static NetworkOperationContext of(TerminalOperationContext open) {
    Objects.requireNonNull(open, "open");
    return new NetworkOperationContext(open.world(), open.terminal(), open.player().getUuid());
  }

  public TerminalAddress address() {
    return TerminalAddress.of(world, terminal);
  }

  public MinecraftServer server() {
    return world.getServer();
  }

  public Optional<NetworkMembership> network() {
    return TerminalNetworks.power().network(world, terminal);
  }

  public TerminalSettings settings() {
    return TerminalLookup.settingsAt(world, terminal).orElseGet(TerminalSettings::defaults);
  }

  public List<StorageOrigin> containers() {
    return network().map(NetworkMembership::containers).orElseGet(List::of);
  }

  public ContainerRole roleFor(StorageOrigin origin) {
    return roleOf(settings(), origin);
  }

  private static ContainerRole roleOf(TerminalSettings settings, StorageOrigin origin) {
    return origin instanceof BlockStorageOrigin block
        ? settings.roleFor(block.pos())
        : ContainerRole.DEFAULT;
  }
}
