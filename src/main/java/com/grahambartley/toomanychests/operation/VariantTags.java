package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class VariantTags {
  private VariantTags() {}

  public static Set<String> of(NetworkTagSet tags, FilterCandidate candidate) {
    Objects.requireNonNull(candidate, "candidate");
    final Set<String> filed = new LinkedHashSet<>(candidate.tags());
    if (tags != null) {
      filed.addAll(tags.tagIdsFor(candidate.identifier()));
    }
    return Set.copyOf(filed);
  }
}
