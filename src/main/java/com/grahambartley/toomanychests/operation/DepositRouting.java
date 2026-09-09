package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

public final class DepositRouting {
  private static final int FILTERED_HOLDING = 0;
  private static final int FILTERED_EMPTY = 1;
  private static final int GENERAL_HOLDING = 2;
  private static final int GENERAL_OTHER = 3;

  private DepositRouting() {}

  public static List<DepositTarget> order(
      List<DepositTarget> targets,
      BlockPos terminal,
      String itemId,
      Collection<String> itemTagIds) {
    Objects.requireNonNull(terminal, "terminal");
    if (targets == null || targets.isEmpty()) {
      return List.of();
    }
    final Collection<String> tags = itemTagIds == null ? Set.of() : itemTagIds;
    return targets.stream()
        .filter(Objects::nonNull)
        .filter(target -> target.accepts(itemId, tags))
        .sorted(
            Comparator.comparingInt(DepositRouting::rung)
                .thenComparingDouble(target -> terminal.getSquaredDistance(target.container()))
                .thenComparingLong(target -> target.container().asLong()))
        .toList();
  }

  private static int rung(DepositTarget target) {
    final boolean filtered = target.role() == ContainerRole.FILTERED;
    if (filtered) {
      return target.holdsItem() ? FILTERED_HOLDING : FILTERED_EMPTY;
    }
    return target.holdsItem() ? GENERAL_HOLDING : GENERAL_OTHER;
  }
}
