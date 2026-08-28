package com.grahambartley.toomanychests.transaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;

/** A slotted container of {@link FakeSlot}s, standing in for a chest. */
final class FakeContainer extends CombinedSlottedStorage<String, FakeSlot> {
  private final List<FakeSlot> slots;

  private FakeContainer(List<FakeSlot> slots) {
    super(slots);
    this.slots = slots;
  }

  static FakeContainer withSlots(int slotCount, long slotCapacity) {
    return new FakeContainer(
        IntStream.range(0, slotCount).mapToObj(slot -> new FakeSlot(slotCapacity)).toList());
  }

  /** Fills slots directly, which is world generation rather than a transfer. */
  FakeContainer holding(String resource, long amount) {
    long remaining = amount;
    for (final FakeSlot slot : slots) {
      if (remaining <= 0L) {
        break;
      }
      if (!slot.isResourceBlank()) {
        continue;
      }
      final long placed = Math.min(remaining, slot.getCapacity());
      slot.set(resource, placed);
      remaining -= placed;
    }
    if (remaining > 0L) {
      throw new IllegalArgumentException("Container cannot hold " + amount + " of " + resource);
    }
    return this;
  }

  long amountOf(String resource) {
    return slots.stream()
        .filter(slot -> !slot.isResourceBlank() && slot.getResource().equals(resource))
        .mapToLong(FakeSlot::getAmount)
        .sum();
  }

  long total() {
    return slots.stream().mapToLong(FakeSlot::getAmount).sum();
  }

  /** Slot by slot, so a test can prove nothing moved rather than only that the totals match. */
  Map<Integer, ResourceEntry> contents() {
    final Map<Integer, ResourceEntry> snapshot = new LinkedHashMap<>();
    for (int slot = 0; slot < slots.size(); slot++) {
      snapshot.put(
          slot, new ResourceEntry(slots.get(slot).getResource(), slots.get(slot).getAmount()));
    }
    return snapshot;
  }

  record ResourceEntry(String resource, long amount) {}
}
