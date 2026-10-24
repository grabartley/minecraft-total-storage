package com.grahambartley.totalstorage.sort;

@FunctionalInterface
public interface SortCandidates<V> {
  SortCandidate of(V variant, long count);
}
