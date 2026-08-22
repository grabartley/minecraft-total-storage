package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.tag.PresetTag;
import java.util.Set;

/** Builders for the holdings sort tests reason about, so cases stay short and comparable. */
final class TestCandidates {
  static final int STACK = 64;

  private TestCandidates() {}

  static SortCandidate of(PresetTag category, String identifier, long count) {
    return of(category, identifier, "", identifier, count, STACK);
  }

  static SortCandidate named(PresetTag category, String identifier, String displayName) {
    return of(category, identifier, "", displayName, STACK, STACK);
  }

  static SortCandidate of(
      PresetTag category,
      String identifier,
      String componentsKey,
      String displayName,
      long count,
      int maxCount) {
    return new SortCandidate(
        category == null ? Set.of() : Set.of(category),
        identifier,
        componentsKey,
        displayName,
        count,
        maxCount);
  }
}
