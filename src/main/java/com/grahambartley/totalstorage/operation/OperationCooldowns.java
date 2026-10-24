package com.grahambartley.totalstorage.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class OperationCooldowns {
  private final Map<UUID, Map<ExpensiveOperation, Long>> lastRun = new HashMap<>();

  public boolean claim(UUID player, ExpensiveOperation operation, long tick, int cooldownTicks) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(operation, "operation");
    if (cooldownTicks <= 0) {
      return true;
    }
    final Map<ExpensiveOperation, Long> byOperation =
        lastRun.computeIfAbsent(player, key -> new HashMap<>());
    final Long previous = byOperation.get(operation);
    if (previous != null && tick - previous < cooldownTicks && tick >= previous) {
      return false;
    }
    byOperation.put(operation, tick);
    return true;
  }

  public long remaining(UUID player, ExpensiveOperation operation, long tick, int cooldownTicks) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(operation, "operation");
    final Map<ExpensiveOperation, Long> byOperation = lastRun.get(player);
    final Long previous = byOperation == null ? null : byOperation.get(operation);
    if (cooldownTicks <= 0 || previous == null || tick < previous) {
      return 0L;
    }
    return Math.max(0L, cooldownTicks - (tick - previous));
  }

  public void forget(UUID player) {
    lastRun.remove(player);
  }

  public void clear() {
    lastRun.clear();
  }

  public boolean isEmpty() {
    return lastRun.isEmpty();
  }
}
