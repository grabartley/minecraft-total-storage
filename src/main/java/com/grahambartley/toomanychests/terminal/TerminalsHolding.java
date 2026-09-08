package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Which running terminals have any of these containers in their network.
 *
 * <p>Radii overlap, and a chest inside two of them belongs to both networks at once. A move made
 * through one terminal is therefore a move the other terminal's listing is now wrong about, and
 * answering only the terminal that acted would leave the second showing a count that no longer
 * exists until something else disturbed it.
 *
 * <p>Terminals that hold none of them are left out rather than told and asked to work out that
 * nothing concerns them, which is what keeps a change to one chest from costing a walk of every
 * network on the server.
 *
 * <p>Generic over the network type per ADR 0012, so the matching is unit testable without a world.
 *
 * @param <N> the network type, {@code NetworkMembership} in production
 */
public final class TerminalsHolding<N> {
  private final TerminalNetworkRegistry<N> registry;
  private final Function<N, Collection<StorageOrigin>> containersOf;

  public TerminalsHolding(
      TerminalNetworkRegistry<N> registry, Function<N, Collection<StorageOrigin>> containersOf) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.containersOf = Objects.requireNonNull(containersOf, "containersOf");
  }

  /** The addresses of every running terminal whose network holds at least one of these. */
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
