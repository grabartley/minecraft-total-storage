package com.grahambartley.toomanychests.screenhandler;

import java.util.Locale;
import java.util.Optional;

public enum TerminalPermission {
  USE,

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
