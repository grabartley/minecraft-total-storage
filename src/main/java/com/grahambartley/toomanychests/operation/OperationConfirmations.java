package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class OperationConfirmations {
  public static final int WINDOW_TICKS = 200;

  private final Map<Pending, Long> armed = new HashMap<>();

  public boolean claim(
      UUID player, TerminalAddress terminal, ExpensiveOperation operation, long tick) {
    final Pending pending = new Pending(player, terminal, operation);
    final Long armedAt = armed.get(pending);
    if (armedAt != null && tick >= armedAt && tick - armedAt <= WINDOW_TICKS) {
      armed.remove(pending);
      return true;
    }
    armed.put(pending, tick);
    return false;
  }

  public boolean isArmed(
      UUID player, TerminalAddress terminal, ExpensiveOperation operation, long tick) {
    final Long armedAt = armed.get(new Pending(player, terminal, operation));
    return armedAt != null && tick >= armedAt && tick - armedAt <= WINDOW_TICKS;
  }

  public void forget(UUID player) {
    armed.keySet().removeIf(pending -> pending.player().equals(player));
  }

  public void clear() {
    armed.clear();
  }

  public boolean isEmpty() {
    return armed.isEmpty();
  }

  private record Pending(UUID player, TerminalAddress terminal, ExpensiveOperation operation) {
    private Pending {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(terminal, "terminal");
      Objects.requireNonNull(operation, "operation");
    }
  }
}
