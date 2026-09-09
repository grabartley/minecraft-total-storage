package com.grahambartley.toomanychests.lock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record LockedSlots(Set<Integer> indices) {
  public static final int FIRST_SLOT = 0;
  public static final int SLOT_COUNT = 36;
  public static final int LAST_SLOT = SLOT_COUNT - 1;

  private static final LockedSlots EMPTY = new LockedSlots(Set.of());

  public LockedSlots {
    indices = sanitize(indices);
  }

  public static LockedSlots empty() {
    return EMPTY;
  }

  public static LockedSlots of(Collection<Integer> indices) {
    return indices == null || indices.isEmpty() ? EMPTY : new LockedSlots(new HashSet<>(indices));
  }

  public static boolean isValidSlot(int slot) {
    return slot >= FIRST_SLOT && slot <= LAST_SLOT;
  }

  public boolean isLocked(int slot) {
    return indices.contains(slot);
  }

  public boolean isEmpty() {
    return indices.isEmpty();
  }

  public int count() {
    return indices.size();
  }

  public List<Integer> sortedIndices() {
    return List.copyOf(indices);
  }

  public LockedSlots with(int slot) {
    if (!isValidSlot(slot) || isLocked(slot)) {
      return this;
    }
    final Set<Integer> next = new TreeSet<>(indices);
    next.add(slot);
    return new LockedSlots(next);
  }

  public LockedSlots without(int slot) {
    if (!isLocked(slot)) {
      return this;
    }
    final Set<Integer> next = new TreeSet<>(indices);
    next.remove(slot);
    return new LockedSlots(next);
  }

  public LockedSlots toggled(int slot) {
    return isLocked(slot) ? without(slot) : with(slot);
  }

  private static Set<Integer> sanitize(Collection<Integer> raw) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<Integer> kept = new TreeSet<>();
    for (final Integer index : raw) {
      if (index != null && isValidSlot(index)) {
        kept.add(index);
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
