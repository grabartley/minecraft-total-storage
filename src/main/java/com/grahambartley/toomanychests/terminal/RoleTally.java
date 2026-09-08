package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Collection;

/**
 * How many of a network's containers hold each role.
 *
 * <p>What a player wants to know before they trust the mod with a base is how much of it is
 * currently writable, and a count answers that in one line where a list of fifty positions does
 * not. Per ADR 0004 {@code EXCLUDED} is the role that earns that trust, so it is the number worth
 * being able to read at a glance.
 *
 * @param general containers the network may both write to and reorder
 * @param filtered containers that have declared what they accept
 * @param excluded containers the network will never write to
 */
public record RoleTally(int general, int filtered, int excluded) {
  public static final RoleTally EMPTY = new RoleTally(0, 0, 0);

  public RoleTally {
    if (general < 0 || filtered < 0 || excluded < 0) {
      throw new IllegalArgumentException("a tally cannot count below zero");
    }
  }

  /** Counts a run of roles, which is what a walk of a network's containers produces. */
  public static RoleTally of(Collection<ContainerRole> roles) {
    if (roles == null || roles.isEmpty()) {
      return EMPTY;
    }
    int general = 0;
    int filtered = 0;
    int excluded = 0;
    for (final ContainerRole role : roles) {
      switch (role == null ? ContainerRole.DEFAULT : role) {
        case GENERAL -> general++;
        case FILTERED -> filtered++;
        case EXCLUDED -> excluded++;
      }
    }
    return new RoleTally(general, filtered, excluded);
  }

  public int countOf(ContainerRole role) {
    return switch (role == null ? ContainerRole.DEFAULT : role) {
      case GENERAL -> general;
      case FILTERED -> filtered;
      case EXCLUDED -> excluded;
    };
  }

  public int total() {
    return general + filtered + excluded;
  }

  public boolean isEmpty() {
    return total() == 0;
  }
}
