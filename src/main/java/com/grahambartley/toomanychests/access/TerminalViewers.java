package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TerminalViewers {
  private final Map<TerminalAddress, Set<UUID>> viewers = new ConcurrentHashMap<>();

  public void opened(TerminalAddress terminal, UUID player) {
    if (terminal == null || player == null) {
      return;
    }
    viewers.computeIfAbsent(terminal, ignored -> ConcurrentHashMap.newKeySet()).add(player);
  }

  public void closed(TerminalAddress terminal, UUID player) {
    if (terminal == null || player == null) {
      return;
    }
    viewers.computeIfPresent(
        terminal,
        (ignored, open) -> {
          open.remove(player);
          return open.isEmpty() ? null : open;
        });
  }

  public void closedEverything(UUID player) {
    if (player == null) {
      return;
    }
    for (final TerminalAddress terminal : Set.copyOf(viewers.keySet())) {
      closed(terminal, player);
    }
  }

  public Set<UUID> viewing(TerminalAddress terminal) {
    if (terminal == null) {
      return Set.of();
    }
    final Set<UUID> open = viewers.get(terminal);
    return open == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(open));
  }

  public boolean isViewing(TerminalAddress terminal, UUID player) {
    return viewing(terminal).contains(player);
  }

  public void clear() {
    viewers.clear();
  }
}
