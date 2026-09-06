package com.grahambartley.toomanychests.screenhandler;

import java.util.Locale;
import java.util.Optional;

/**
 * What the player who opened a terminal may do with it, as far as the screen needs to know.
 *
 * <p>Deliberately narrower than {@link com.grahambartley.toomanychests.access.TerminalAccess}: the
 * client is told what to draw, not why it was let in. Which of the trust list, the owner lock or
 * the operator bypass granted access is the server's business, and naming the owner of a base to
 * everyone who can open its terminal would be a leak rather than a feature.
 *
 * <p>Per ADR 0009 this decides rendering only. Every operation is re-checked server-side, so a
 * modified client claiming {@code ADMINISTER} gains nothing but buttons that refuse it.
 */
public enum TerminalPermission {
  /** May read the network and move items through it. */
  USE,
  /** May also move the access mode and edit the trust list. */
  ADMINISTER;

  public boolean mayAdminister() {
    return this == ADMINISTER;
  }

  public static TerminalPermission of(boolean mayAdminister) {
    return mayAdminister ? ADMINISTER : USE;
  }

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<TerminalPermission> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (final TerminalPermission permission : values()) {
      if (permission.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(permission);
      }
    }
    return Optional.empty();
  }
}
