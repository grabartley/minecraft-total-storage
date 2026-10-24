package com.grahambartley.totalstorage.terminal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NetworkCapacityWatch {
  private final Map<TerminalAddress, NetworkCapacityStamp> measuredAt = new HashMap<>();

  public boolean needsMeasuring(TerminalAddress terminal, NetworkCapacityStamp stamp) {
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(stamp, "stamp");
    return !stamp.equals(measuredAt.get(terminal));
  }

  public void measured(TerminalAddress terminal, NetworkCapacityStamp stamp) {
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(stamp, "stamp");
    measuredAt.put(terminal, stamp);
  }

  public void retainOnly(Collection<TerminalAddress> running) {
    measuredAt.keySet().retainAll(running == null ? Set.of() : running);
  }

  public boolean isWatching(TerminalAddress terminal) {
    return measuredAt.containsKey(terminal);
  }

  public int watchedCount() {
    return measuredAt.size();
  }
}
