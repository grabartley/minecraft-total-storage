package com.grahambartley.toomanychests.storage;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;

public record ContainerAssignments(Map<BlockPos, ContainerAssignment> byPosition) {
  private static final ContainerAssignments EMPTY = new ContainerAssignments(Map.of());

  public ContainerAssignments {
    byPosition = canonicalise(byPosition);
  }

  public static ContainerAssignments empty() {
    return EMPTY;
  }

  public ContainerAssignment forContainer(BlockPos container) {
    return container == null
        ? ContainerAssignment.DEFAULT
        : byPosition.getOrDefault(container.toImmutable(), ContainerAssignment.DEFAULT);
  }

  public ContainerRole roleFor(BlockPos container) {
    return forContainer(container).role();
  }

  public ContainerFilter filterFor(BlockPos container) {
    return forContainer(container).filter();
  }

  public boolean isEmpty() {
    return byPosition.isEmpty();
  }

  public int size() {
    return byPosition.size();
  }

  public ContainerAssignments with(BlockPos container, ContainerAssignment assignment) {
    if (container == null) {
      return this;
    }
    final ContainerAssignment assigned =
        assignment == null ? ContainerAssignment.DEFAULT : assignment;
    if (forContainer(container).equals(assigned)) {
      return this;
    }
    final Map<BlockPos, ContainerAssignment> next = new LinkedHashMap<>(byPosition);
    if (assigned.isDefault()) {
      next.remove(container.toImmutable());
    } else {
      next.put(container.toImmutable(), assigned);
    }
    return new ContainerAssignments(next);
  }

  public ContainerAssignments withRole(BlockPos container, ContainerRole role) {
    return with(container, forContainer(container).withRole(role));
  }

  public ContainerAssignments withFilter(BlockPos container, ContainerFilter filter) {
    return with(container, forContainer(container).withFilter(filter));
  }

  public ContainerAssignments without(BlockPos container) {
    return with(container, ContainerAssignment.DEFAULT);
  }

  private static Map<BlockPos, ContainerAssignment> canonicalise(
      Map<BlockPos, ContainerAssignment> raw) {
    if (raw == null || raw.isEmpty()) {
      return Map.of();
    }
    final Map<BlockPos, ContainerAssignment> kept = new LinkedHashMap<>();
    raw.entrySet().stream()
        .filter(entry -> entry.getKey() != null)
        .filter(entry -> entry.getValue() != null && !entry.getValue().isDefault())
        .sorted(Comparator.comparingLong(entry -> entry.getKey().asLong()))
        .forEach(entry -> kept.put(entry.getKey().toImmutable(), entry.getValue()));
    return Collections.unmodifiableMap(kept);
  }
}
