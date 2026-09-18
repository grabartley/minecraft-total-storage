package com.grahambartley.toomanychests.terminal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NetworkCapacityWatch {
  private final Map<TerminalAddress, Integer> measuredAt = new HashMap<>();

  public boolean needsMeasuring(TerminalAddress terminal, int invalidationCount) {
    Objects.requireNonNull(terminal, "terminal");
    final Integer seen = measuredAt.get(terminal);
    return seen == null || seen != invalidationCount;
  }

  public void measured(TerminalAddress terminal, int invalidationCount) {
    Objects.requireNonNull(terminal, "terminal");
    measuredAt.put(terminal, invalidationCount);
  }

  public void forget(TerminalAddress terminal) {
    measuredAt.remove(terminal);
  }

  public void retainOnly(Collection<TerminalAddress> running) {
    measuredAt.keySet().retainAll(running == null ? java.util.Set.of() : running);
  }

  public boolean isWatching(TerminalAddress terminal) {
    return measuredAt.containsKey(terminal);
  }

  public int watchedCount() {
    return measuredAt.size();
  }

  public void clear() {
    measuredAt.clear();
  }
}
