package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

/**
 * Which container should receive an item, and in what order the rest are tried after it.
 *
 * <p>This is the decision that removes the mod's core friction, so it is a pure function over plain
 * data rather than something tangled up with the moves it causes. Nothing here touches a storage,
 * which is what lets every rung of the order be proven without a running world.
 *
 * <p>The order is the one ADR 0004 implies from the roles. A {@code FILTERED} container has already
 * told the network what it wants, so it is preferred over a general one; within either kind, a
 * container that already holds the item is preferred, so stacks gather rather than scatter. {@code
 * EXCLUDED} containers appear at no rung.
 *
 * <p>The whole order is returned rather than a single winner. A container that turns out to be full
 * accepts nothing and the next one takes the remainder, which is what stops a full filtered chest
 * from rejecting an item the network could otherwise hold.
 */
public final class DepositRouting {
  private static final int FILTERED_HOLDING = 0;
  private static final int FILTERED_EMPTY = 1;
  private static final int GENERAL_HOLDING = 2;
  private static final int GENERAL_OTHER = 3;

  private DepositRouting() {}

  /**
   * Every container that will take the item, best first.
   *
   * <p>Ties are broken by distance from the terminal and then by position, so a network deposits
   * the same way twice and a test can say where an item will land.
   *
   * @param terminal the terminal the deposit was made at, which nearness is measured from
   * @param itemId the registry id of the item being deposited
   * @param itemTagIds the tags this network files that item under, presets included
   */
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
