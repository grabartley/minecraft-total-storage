package com.grahambartley.toomanychests.tag;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public enum TagSetImportMode {
  MERGE,

  REPLACE;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public boolean clearsWhatIsThere() {
    return this == REPLACE;
  }

  public static Optional<TagSetImportMode> fromId(String id) {
    return id == null
        ? Optional.empty()
        : Stream.of(values()).filter(mode -> mode.id().equalsIgnoreCase(id.trim())).findFirst();
  }
}
