package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.ArrayList;
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

  public List<StorageOrigin> writableContainers() {
    final TerminalSettings settings = settings();
    final List<StorageOrigin> writable = new ArrayList<>();
    for (final StorageOrigin origin : containers()) {
      if (roleOf(settings, origin) != ContainerRole.EXCLUDED) {
        writable.add(origin);
      }
    }
    return List.copyOf(writable);
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
