package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class TerminalsHolding<N> {
  private final TerminalNetworkRegistry<N> registry;
  private final Function<N, Collection<StorageOrigin>> containersOf;

  public TerminalsHolding(
      TerminalNetworkRegistry<N> registry, Function<N, Collection<StorageOrigin>> containersOf) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.containersOf = Objects.requireNonNull(containersOf, "containersOf");
  }

  public List<TerminalAddress> any(Collection<StorageOrigin> origins) {
    if (origins == null || origins.isEmpty()) {
      return List.of();
    }
    final Set<StorageOrigin> wanted = new HashSet<>(origins);
    final List<TerminalAddress> holding = new ArrayList<>();
    for (final TerminalAddress address : registry.addresses()) {
      final N network = registry.find(address).orElse(null);
      if (network != null && holdsAny(network, wanted)) {
        holding.add(address);
      }
    }
    return List.copyOf(holding);
  }

  private boolean holdsAny(N network, Set<StorageOrigin> wanted) {
    final Collection<StorageOrigin> containers = containersOf.apply(network);
    if (containers == null) {
      return false;
    }
    for (final StorageOrigin container : containers) {
      if (wanted.contains(container)) {
        return true;
      }
    }
    return false;
  }
}
