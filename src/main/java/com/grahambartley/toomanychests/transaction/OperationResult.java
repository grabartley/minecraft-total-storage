package com.grahambartley.toomanychests.transaction;

import java.util.Objects;
import java.util.Optional;

/**
 * What a network operation decided, and the value it produced if it decided to commit.
 *
 * <p>Deciding is the operation's job rather than the runner's, because only the operation knows
 * whether what it managed to move is enough. A withdrawal that finds thirty of the sixty-four items
 * it asked for has succeeded; one that inserts thirty and extracts twenty has not.
 *
 * @param <T> what the operation produces, commonly the amount moved
 */
public record OperationResult<T>(boolean committed, Optional<T> value, String reason) {
  public OperationResult {
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(reason, "reason");
    if (committed && value.isEmpty()) {
      throw new IllegalArgumentException("A committed result must carry a value");
    }
    if (!committed && reason.isBlank()) {
      throw new IllegalArgumentException("A rolled back result must carry a reason");
    }
  }

  public static <T> OperationResult<T> committed(T value) {
    return new OperationResult<>(true, Optional.of(Objects.requireNonNull(value, "value")), "");
  }

  public static <T> OperationResult<T> rolledBack(String reason) {
    return new OperationResult<>(false, Optional.empty(), reason);
  }

  public boolean rolledBack() {
    return !committed;
  }

  public T orElse(T fallback) {
    return value.orElse(fallback);
  }
}
