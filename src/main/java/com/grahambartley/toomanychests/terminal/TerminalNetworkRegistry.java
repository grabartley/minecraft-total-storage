package com.grahambartley.toomanychests.terminal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TerminalNetworkRegistry<N> {
  private final Map<TerminalAddress, N> active = new LinkedHashMap<>();

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

  public List<N> clear() {
    final List<N> cleared = new ArrayList<>(active.values());
    active.clear();
    return List.copyOf(cleared);
  }
}
