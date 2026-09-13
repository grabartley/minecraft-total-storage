package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.SortOrdering;
import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.transaction.FakeSlot;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

class SortArrangementTest {
  private static final int STACK = 64;

  private static final Comparator<SortCandidate> BY_REGISTRY_ID =
      SortComparator.forOrdering(SortOrdering.REGISTRY_ID);

  private static final SortCandidates<String> CANDIDATES =
      (variant, count) -> TestCandidates.of(PresetTag.STONE, variant, "", variant, count, STACK);

  private static List<FakeSlot> container(int slots) {
    final List<FakeSlot> made = new ArrayList<>(slots);
    for (int slot = 0; slot < slots; slot++) {
      made.add(new FakeSlot(STACK));
    }
    return made;
  }

  private static List<String> contentsOf(List<FakeSlot> slots) {
    return slots.stream()
        .map(slot -> slot.isResourceBlank() ? "-" : slot.getResource() + "x" + slot.getAmount())
        .toList();
  }

  private static long totalIn(List<FakeSlot> slots) {
    return slots.stream().mapToLong(FakeSlot::getAmount).sum();
  }

  private static <T> T committed(Function<TransactionContext, T> work) {
    try (Transaction transaction = Transaction.openOuter()) {
      final T result = work.apply(transaction);
      transaction.commit();
      return result;
    }
  }

  private static SortPlan<String> planFor(List<FakeSlot> slots) {
    return SortArrangement.of(slots, STACK, CANDIDATES, BY_REGISTRY_ID);
  }

  @Test
  void anEmptyContainerPlansNothing() {
    assertTrue(planFor(container(3)).isEmpty());
  }

  @Test
  void partStacksOfOneItemArePlannedAsOneStack() {
    List<FakeSlot> slots = container(4);
    slots.get(0).set("cobblestone", 30L);
    slots.get(2).set("cobblestone", 20L);

    SortPlan<String> plan = planFor(slots);

    assertEquals(1, plan.size());
    assertEquals(50L, plan.total());
  }

  @Test
  void anItemBeyondOneStackIsPlannedAsAFullStackAndARemainder() {
    List<FakeSlot> slots = container(4);
    slots.get(0).set("cobblestone", 64L);
    slots.get(1).set("cobblestone", 10L);

    SortPlan<String> plan = planFor(slots);

    assertEquals(List.of(64L, 10L), plan.stacks().stream().map(SortHolding::count).toList());
  }

  @Test
  void aContainerAlreadyHoldingTheArrangementMatchesIt() {
    List<FakeSlot> slots = container(3);
    slots.get(0).set("cobblestone", 50L);

    assertTrue(SortArrangement.matches(slots, planFor(slots)));
  }

  @Test
  void aContainerWithGapsDoesNotMatchTheArrangement() {
    List<FakeSlot> slots = container(3);
    slots.get(1).set("cobblestone", 50L);

    assertFalse(SortArrangement.matches(slots, planFor(slots)));
  }

  @Test
  void aContainerWithScatteredPartStacksDoesNotMatchTheArrangement() {
    List<FakeSlot> slots = container(3);
    slots.get(0).set("cobblestone", 30L);
    slots.get(1).set("cobblestone", 20L);

    assertFalse(SortArrangement.matches(slots, planFor(slots)));
  }

  @Test
  void applyingAnArrangementCompactsTheContainerAndConservesEverything() {
    List<FakeSlot> slots = container(5);
    slots.get(0).set("stone", 10L);
    slots.get(2).set("cobblestone", 30L);
    slots.get(4).set("cobblestone", 20L);
    long before = totalIn(slots);

    OperationResult<Long> result =
        committed(transaction -> SortArrangement.apply(slots, planFor(slots), transaction));

    assertTrue(result.committed());
    assertEquals(List.of("cobblestonex50", "stonex10", "-", "-", "-"), contentsOf(slots));
    assertEquals(before, totalIn(slots));
  }

  @Test
  void applyingAnArrangementTwiceLeavesTheSecondPassWithNothingToDo() {
    List<FakeSlot> slots = container(4);
    slots.get(1).set("cobblestone", 12L);
    slots.get(3).set("cobblestone", 5L);

    committed(transaction -> SortArrangement.apply(slots, planFor(slots), transaction));

    assertTrue(SortArrangement.matches(slots, planFor(slots)));
  }

  @Test
  void anArrangementNeedingMoreSlotsThanTheContainerHasIsRolledBack() {
    List<FakeSlot> wide = container(3);
    wide.get(0).set("stone", 1L);
    wide.get(1).set("cobblestone", 1L);
    wide.get(2).set("coal", 1L);
    SortPlan<String> plan = planFor(wide);
    List<FakeSlot> narrow = container(2);

    OperationResult<Long> result =
        committed(transaction -> SortArrangement.apply(narrow, plan, transaction));

    assertTrue(result.rolledBack());
    assertEquals(0L, totalIn(narrow));
  }

  @Test
  void applyingAnEmptyPlanIsRolledBackRatherThanEmptyingTheContainer() {
    List<FakeSlot> slots = container(2);
    slots.get(0).set("stone", 4L);

    OperationResult<Long> result =
        committed(transaction -> SortArrangement.apply(slots, SortPlan.nothing(), transaction));

    assertTrue(result.rolledBack());
    assertEquals(4L, totalIn(slots));
  }
}
