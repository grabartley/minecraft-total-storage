package com.grahambartley.toomanychests.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * How recently each player last ran each expensive operation, and whether they may run it again.
 *
 * <p>Holds no clock and reads no config. The tick and the cooldown are handed in, which is what
 * lets every boundary here be proven without a server: a cooldown that is only ever exercised
 * against a running game is a cooldown whose off-by-one nobody finds until a player reports that
 * the button eats every other click.
 *
 * <p>Keyed per player and per operation. A shared counter would make emptying an inventory spend a
 * rescan, which reads as the mod ignoring a button rather than as a limit doing its job.
 *
 * <p>Entries are dropped when a player leaves, so a long-running server does not accumulate a row
 * per player who ever logged in.
 */
public final class OperationCooldowns {
  private final Map<UUID, Map<ExpensiveOperation, Long>> lastRun = new HashMap<>();

  /**
   * Whether the operation may run now, recording it when it may.
   *
   * <p>Asking is what spends the cooldown, so a caller cannot check and then forget to act. A
   * cooldown of zero always allows, which is what makes the setting's floor mean "off" rather than
   * "one per tick".
   */
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

  /** How many ticks are left before the operation may run again, or zero when it may run now. */
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

  /** Forgets a player, for when they disconnect. */
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
