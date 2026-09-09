package com.grahambartley.toomanychests.tag;

import java.util.Objects;

public record TagEditResult(TagEditOutcome outcome, TagNameOutcome name, int affected) {
  public TagEditResult {
    Objects.requireNonNull(outcome, "outcome");
    name = name == null ? TagNameOutcome.OK : name;
    affected = Math.max(0, affected);
  }

  public static TagEditResult of(TagEditOutcome outcome) {
    return new TagEditResult(outcome, TagNameOutcome.OK, 0);
  }

  public static TagEditResult changed(int affected) {
    return new TagEditResult(TagEditOutcome.CHANGED, TagNameOutcome.OK, affected);
  }

  public static TagEditResult rejected(TagNameOutcome name) {
    return new TagEditResult(TagEditOutcome.NAME_REJECTED, name, 0);
  }

  public boolean isChange() {
    return outcome.isChange();
  }
}
