package com.grahambartley.toomanychests.storage;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;

/**
 * Every decision one network has made about the containers it can see. See ADR 0004.
 *
 * <p>Assignments are keyed by position rather than by container identity, because a chest has no
 * identity that survives being broken. A container that is removed has its assignment forgotten as
 * it goes, which is what stops the next container placed there inheriting decisions made about a
 * different one; see ADR 0014 for why removal is the only event that can be trusted for this.
 *
 * <p>A container holding the default assignment is forgotten rather than recorded, so a network
 * nobody has configured costs nothing to store. Order is by position rather than by the order edits
 * arrived in, so two networks configured the same way compare equal and encode identically.
 *
 * @param byPosition the containers this network has an opinion about, in canonical position order
 */
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

  /** Drops a container entirely, which is what a container leaving the world for good earns. */
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
