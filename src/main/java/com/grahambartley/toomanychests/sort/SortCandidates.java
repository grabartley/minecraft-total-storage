package com.grahambartley.toomanychests.sort;

@FunctionalInterface
public interface SortCandidates<V> {
  SortCandidate of(V variant, long count);
}
