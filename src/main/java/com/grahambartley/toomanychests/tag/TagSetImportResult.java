package com.grahambartley.toomanychests.tag;

import java.util.Objects;

public record TagSetImportResult(NetworkTagSet tags, int imported, int skipped) {
  public TagSetImportResult {
    Objects.requireNonNull(tags, "tags");
    imported = Math.max(0, imported);
    skipped = Math.max(0, skipped);
  }

  public boolean broughtSomethingAcross() {
    return imported > 0;
  }
}
