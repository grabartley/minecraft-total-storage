package com.grahambartley.toomanychests.tag;

import java.util.Objects;

public record TagTransferResult(TagEditOutcome outcome, int imported, int skipped) {
  public TagTransferResult {
    Objects.requireNonNull(outcome, "outcome");
    imported = Math.max(0, imported);
    skipped = Math.max(0, skipped);
  }

  public static TagTransferResult of(TagEditOutcome outcome) {
    return new TagTransferResult(outcome, 0, 0);
  }

  public static TagTransferResult from(TagSetImportResult result, boolean stored) {
    if (!stored) {
      return new TagTransferResult(TagEditOutcome.UNCHANGED, 0, result.skipped());
    }
    return new TagTransferResult(TagEditOutcome.CHANGED, result.imported(), result.skipped());
  }

  public boolean isChange() {
    return outcome.isChange();
  }
}
