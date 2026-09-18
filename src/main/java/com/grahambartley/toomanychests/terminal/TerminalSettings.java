package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerAssignments;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;

public record TerminalSettings(
    int radius,
    TerminalAccessMode accessMode,
    Optional<UUID> owner,
    Set<UUID> trusted,
    ContainerAssignments containers,
    NetworkTagSet tags) {
  public static final int MIN_RADIUS = TooManyChestsConfig.MAX_TERMINAL_RADIUS_MIN;
  public static final int MAX_RADIUS = TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX;

  public static final int DEFAULT_RADIUS = TooManyChestsConfig.DEFAULT_MAX_TERMINAL_RADIUS;

  private static final TerminalSettings DEFAULTS =
      new TerminalSettings(
          DEFAULT_RADIUS,
          TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE,
          Optional.empty(),
          Set.of(),
          ContainerAssignments.empty(),
          NetworkTagSet.defaults());

  public TerminalSettings {
    radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    accessMode = accessMode == null ? TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE : accessMode;
    owner = owner == null ? Optional.empty() : owner;
    trusted = sanitizeTrusted(trusted);
    containers = containers == null ? ContainerAssignments.empty() : containers;
    tags = tags == null ? NetworkTagSet.defaults() : tags;
  }

  public static TerminalSettings defaults() {
    return DEFAULTS;
  }

  public TerminalSettings withRadius(int newRadius) {
    return new TerminalSettings(newRadius, accessMode, owner, trusted, containers, tags);
  }

  public TerminalSettings withAccessMode(TerminalAccessMode newAccessMode) {
    return new TerminalSettings(radius, newAccessMode, owner, trusted, containers, tags);
  }

  public TerminalSettings withOwner(UUID newOwner) {
    return new TerminalSettings(
        radius, accessMode, Optional.ofNullable(newOwner), trusted, containers, tags);
  }

  public boolean isOwnedBy(UUID candidate) {
    return candidate != null && owner.map(candidate::equals).orElse(false);
  }

  public boolean isTrusted(UUID candidate) {
    return candidate != null && trusted.contains(candidate);
  }

  public TerminalSettings withTrusted(UUID player) {
    if (player == null || trusted.contains(player)) {
      return this;
    }
    final Set<UUID> next = newTrustedSet();
    next.addAll(trusted);
    next.add(player);
    return new TerminalSettings(radius, accessMode, owner, next, containers, tags);
  }

  public TerminalSettings withoutTrusted(UUID player) {
    if (player == null || !trusted.contains(player)) {
      return this;
    }
    final Set<UUID> next = newTrustedSet();
    next.addAll(trusted);
    next.remove(player);
    return new TerminalSettings(radius, accessMode, owner, next, containers, tags);
  }

  public ContainerAssignment assignmentFor(BlockPos container) {
    return containers.forContainer(container);
  }

  public ContainerRole roleFor(BlockPos container) {
    return containers.roleFor(container);
  }

  public ContainerFilter filterFor(BlockPos container) {
    return containers.filterFor(container);
  }

  public String nameFor(BlockPos container) {
    return containers.nameFor(container);
  }

  public TerminalSettings withContainers(ContainerAssignments updated) {
    if (updated == null || updated.equals(containers)) {
      return this;
    }
    return new TerminalSettings(radius, accessMode, owner, trusted, updated, tags);
  }

  public TerminalSettings withContainerRole(BlockPos container, ContainerRole role) {
    return withContainers(containers.withRole(container, role));
  }

  public TerminalSettings withContainerFilter(BlockPos container, ContainerFilter filter) {
    return withContainers(containers.withFilter(container, filter));
  }

  public TerminalSettings forgetContainer(BlockPos container) {
    return withContainers(containers.without(container));
  }

  public boolean hasTag(String tag) {
    return tags.contains(tag);
  }

  public TerminalSettings withTags(NetworkTagSet updated) {
    if (updated == null || updated.equals(tags)) {
      return this;
    }
    return new TerminalSettings(radius, accessMode, owner, trusted, containers, updated);
  }

  public TerminalSettings withTag(String tag) {
    return withTags(tags.withCustomTag(tag));
  }

  public TerminalSettings withoutTag(String tag) {
    return withTags(tags.withoutTag(tag));
  }

  private static Set<UUID> newTrustedSet() {
    return new TreeSet<>(Comparator.comparing(UUID::toString));
  }

  private static Set<UUID> sanitizeTrusted(Collection<UUID> raw) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<UUID> kept = newTrustedSet();
    for (final UUID player : raw) {
      if (player != null) {
        kept.add(player);
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
