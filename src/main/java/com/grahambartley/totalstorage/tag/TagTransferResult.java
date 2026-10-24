package com.grahambartley.totalstorage.tag;

import java.util.Objects;

public record TagTransferResult(TagEditOutcome outcome, int imported, TagSkips skips) {
  public TagTransferResult {
    Objects.requireNonNull(outcome, "outcome");
    imported = Math.max(0, imported);
    skips = skips == null ? TagSkips.NONE : skips;
  }

  public static TagTransferResult of(TagEditOutcome outcome) {
    return new TagTransferResult(outcome, 0, TagSkips.NONE);
  }

  public static TagTransferResult from(TagSetImportResult result, boolean stored) {
    if (!stored) {
      return new TagTransferResult(TagEditOutcome.UNCHANGED, 0, result.skips());
    }
    return new TagTransferResult(TagEditOutcome.CHANGED, result.imported(), result.skips());
  }

  public int skipped() {
    return skips.total();
  }

  public boolean isChange() {
    return outcome.isChange();
  }
}
