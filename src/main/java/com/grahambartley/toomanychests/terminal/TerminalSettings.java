package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;

/**
 * Everything a storage network remembers about itself: its reach, the roles it has given the
 * containers it can see, its tag set, and who may operate it. See ADR 0004, ADR 0006 and ADR 0008.
 *
 * <p>These live on the terminal's block entity rather than in world state, which is what makes
 * cutting a terminal's power an off switch rather than a factory reset.
 *
 * <p>Every value is sanitized on construction rather than at each reader, so a hand-edited save
 * cannot put a radius, a role or a blank tag in front of code that assumes none of them can happen.
 */
public record TerminalSettings(
    int radius,
    TerminalAccessMode accessMode,
    Optional<UUID> owner,
    Set<UUID> trusted,
    Map<BlockPos, ContainerRole> containerRoles,
    Set<String> tags) {

  public static final int MIN_RADIUS = TooManyChestsConfig.MAX_TERMINAL_RADIUS_MIN;
  public static final int MAX_RADIUS = TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX;

  /**
   * A fresh terminal reaches as far as a default server permits. The live server maximum is a clamp
   * applied on top rather than a starting value, so this stays a plain constant.
   */
  public static final int DEFAULT_RADIUS = TooManyChestsConfig.DEFAULT_MAX_TERMINAL_RADIUS;

  private static final TerminalSettings DEFAULTS =
      new TerminalSettings(
          DEFAULT_RADIUS,
          TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE,
          Optional.empty(),
          Set.of(),
          Map.of(),
          Set.of());

  public TerminalSettings {
    radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    accessMode = accessMode == null ? TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE : accessMode;
    owner = owner == null ? Optional.empty() : owner;
    trusted = sanitizeTrusted(trusted);
    containerRoles = sanitizeRoles(containerRoles);
    tags = sanitizeTags(tags);
  }

  public static TerminalSettings defaults() {
    return DEFAULTS;
  }

  public TerminalSettings withRadius(int newRadius) {
    return new TerminalSettings(newRadius, accessMode, owner, trusted, containerRoles, tags);
  }

  public TerminalSettings withAccessMode(TerminalAccessMode newAccessMode) {
    return new TerminalSettings(radius, newAccessMode, owner, trusted, containerRoles, tags);
  }

  public TerminalSettings withOwner(UUID newOwner) {
    return new TerminalSettings(
        radius, accessMode, Optional.ofNullable(newOwner), trusted, containerRoles, tags);
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
    return new TerminalSettings(radius, accessMode, owner, next, containerRoles, tags);
  }

  public TerminalSettings withoutTrusted(UUID player) {
    if (player == null || !trusted.contains(player)) {
      return this;
    }
    final Set<UUID> next = newTrustedSet();
    next.addAll(trusted);
    next.remove(player);
    return new TerminalSettings(radius, accessMode, owner, next, containerRoles, tags);
  }

  public ContainerRole roleFor(BlockPos container) {
    return container == null
        ? ContainerRole.DEFAULT
        : containerRoles.getOrDefault(container.toImmutable(), ContainerRole.DEFAULT);
  }

  /**
   * Assigning the default role forgets the container rather than recording it, so a network that
   * has never been configured costs nothing to store and two equally configured networks compare
   * equal whatever order got them there.
   */
  public TerminalSettings withContainerRole(BlockPos container, ContainerRole role) {
    if (container == null) {
      return this;
    }
    final ContainerRole assigned = role == null ? ContainerRole.DEFAULT : role;
    if (roleFor(container) == assigned) {
      return this;
    }
    final Map<BlockPos, ContainerRole> next = new LinkedHashMap<>(containerRoles);
    if (assigned == ContainerRole.DEFAULT) {
      next.remove(container.toImmutable());
    } else {
      next.put(container.toImmutable(), assigned);
    }
    return new TerminalSettings(radius, accessMode, owner, trusted, next, tags);
  }

  public boolean hasTag(String tag) {
    return tag != null && tags.contains(tag.trim());
  }

  public TerminalSettings withTag(String tag) {
    if (tag == null || tag.isBlank() || hasTag(tag)) {
      return this;
    }
    final Set<String> next = new TreeSet<>(tags);
    next.add(tag.trim());
    return new TerminalSettings(radius, accessMode, owner, trusted, containerRoles, next);
  }

  public TerminalSettings withoutTag(String tag) {
    if (!hasTag(tag)) {
      return this;
    }
    final Set<String> next = new TreeSet<>(tags);
    next.remove(tag.trim());
    return new TerminalSettings(radius, accessMode, owner, trusted, containerRoles, next);
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

  private static Map<BlockPos, ContainerRole> sanitizeRoles(Map<BlockPos, ContainerRole> raw) {
    if (raw == null || raw.isEmpty()) {
      return Map.of();
    }
    final Map<BlockPos, ContainerRole> kept = new LinkedHashMap<>();
    raw.entrySet().stream()
        .filter(entry -> entry.getKey() != null)
        .filter(entry -> entry.getValue() != null && entry.getValue() != ContainerRole.DEFAULT)
        .sorted(Comparator.comparingLong(entry -> entry.getKey().asLong()))
        .forEach(entry -> kept.put(entry.getKey().toImmutable(), entry.getValue()));
    return Collections.unmodifiableMap(kept);
  }

  private static Set<String> sanitizeTags(Collection<String> raw) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    final Set<String> kept = new TreeSet<>();
    for (final String tag : raw) {
      if (tag != null && !tag.isBlank()) {
        kept.add(tag.trim());
      }
    }
    return Collections.unmodifiableSet(kept);
  }
}
