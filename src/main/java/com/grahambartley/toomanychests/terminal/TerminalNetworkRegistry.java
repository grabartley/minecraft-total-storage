package com.grahambartley.toomanychests.terminal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Which terminals are powered, and the network each of them is running.
 *
 * <p>Per ADR 0007 an unpowered terminal is absent rather than idle, so absence from this registry
 * is the whole of what "unpowered" means to the rest of the mod. Anything asking a terminal for its
 * network asks here, and an empty answer is the normal one.
 *
 * <p>Server side and single threaded, like the block events that drive it.
 *
 * @param <N> the network type, {@code NetworkMembership} in production
 */
public final class TerminalNetworkRegistry<N> {
  private final Map<TerminalAddress, N> active = new LinkedHashMap<>();

  /** Returns whatever was running at that address before, which the caller must shut down. */
  public Optional<N> activate(TerminalAddress address, N network) {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(network, "network");
    return Optional.ofNullable(active.put(address, network));
  }

  public Optional<N> deactivate(TerminalAddress address) {
    return address == null ? Optional.empty() : Optional.ofNullable(active.remove(address));
  }

  public Optional<N> find(TerminalAddress address) {
    return address == null ? Optional.empty() : Optional.ofNullable(active.get(address));
  }

  public boolean isActive(TerminalAddress address) {
    return address != null && active.containsKey(address);
  }

  public int activeCount() {
    return active.size();
  }

  public List<TerminalAddress> addresses() {
    return List.copyOf(active.keySet());
  }

  /**
   * Empties the registry and hands back every network, so none is torn down twice or not at all.
   */
  public List<N> clear() {
    final List<N> cleared = new ArrayList<>(active.values());
    active.clear();
    return List.copyOf(cleared);
  }
}
