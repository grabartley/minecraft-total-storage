package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

public final class DepositRouting {
  private static final int GENERAL_OFFSET = ContainerContents.values().length;

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
    final int offset = target.role() == ContainerRole.FILTERED ? 0 : GENERAL_OFFSET;
    return offset + target.contents().preference();
  }
}
