package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who currently has a terminal open.
 *
 * <p>Access is checked on every operation rather than only on opening, per ADR 0008, but a player
 * who has lost access should not be left staring at a screen that refuses everything either. This
 * is the record that lets a revoked player be shut out of the interface at the moment it happens
 * rather than at their next click.
 *
 * <p>Players are held by UUID rather than by entity so a viewer who logs out mid-operation cannot
 * keep a player object alive, and a terminal nobody is looking at holds no entry at all.
 */
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

  /** What a player logging out closes, since no screen will tell us about it afterwards. */
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
