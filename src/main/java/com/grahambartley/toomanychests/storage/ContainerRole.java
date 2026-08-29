package com.grahambartley.toomanychests.storage;

import java.util.Locale;
import java.util.Optional;

/** What a connected container is allowed to have done to it. See ADR 0004. */
public enum ContainerRole {
  GENERAL,
  FILTERED,
  EXCLUDED;

  public static final ContainerRole DEFAULT = GENERAL;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<ContainerRole> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (ContainerRole role : values()) {
      if (role.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(role);
      }
    }
    return Optional.empty();
  }
}
