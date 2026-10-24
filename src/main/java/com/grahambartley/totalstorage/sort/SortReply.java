package com.grahambartley.totalstorage.sort;

import java.util.Objects;

public record SortReply(SortSubject subject, SortOutcome outcome) {
  public SortReply {
    Objects.requireNonNull(subject, "subject");
    Objects.requireNonNull(outcome, "outcome");
  }
}
