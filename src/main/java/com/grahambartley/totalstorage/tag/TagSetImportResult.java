package com.grahambartley.totalstorage.tag;

import java.util.Objects;

public record TagSetImportResult(NetworkTagSet tags, int imported, TagSkips skips) {
  public TagSetImportResult {
    Objects.requireNonNull(tags, "tags");
    imported = Math.max(0, imported);
    skips = skips == null ? TagSkips.NONE : skips;
  }

  public int skipped() {
    return skips.total();
  }

  public boolean broughtSomethingAcross() {
    return imported > 0;
  }
}
