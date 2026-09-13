package com.grahambartley.toomanychests.config;

import java.util.Locale;
import java.util.Optional;

public enum ScreenButtonAnchor implements ConfigEnum {
  INSIDE_TOP_RIGHT,
  OUTSIDE_TOP_RIGHT,
  OUTSIDE_BOTTOM_RIGHT;

  @Override
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<ScreenButtonAnchor> fromId(String id) {
    if (id == null) {
      return Optional.empty();
    }
    for (final ScreenButtonAnchor anchor : values()) {
      if (anchor.id().equalsIgnoreCase(id.trim())) {
        return Optional.of(anchor);
      }
    }
    return Optional.empty();
  }
}
