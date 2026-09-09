package com.grahambartley.toomanychests.config;

import java.util.Locale;
import java.util.Optional;

public enum TerminalAccessMode implements ConfigEnum {
  OWNER,
  TRUSTED,
  OPEN;

  @Override
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<TerminalAccessMode> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (TerminalAccessMode mode : values()) {
      if (mode.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(mode);
      }
    }
    return Optional.empty();
  }
}
