package com.grahambartley.toomanychests.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * A search box's contents, already split into the three things it can ask for.
 *
 * <p>Name and tag terms narrow while mod identifier terms widen, per ADR 0013.
 *
 * @param nameTerms bare text, matched against display name and item identifier
 * @param tagTerms terms entered with a {@code #} prefix, matched against the item's tags
 * @param modIdTerms terms entered with an {@code @} prefix, matched against the source mod
 */
public record SearchQuery(List<String> nameTerms, List<String> tagTerms, List<String> modIdTerms) {
  public static final SearchQuery EMPTY = new SearchQuery(List.of(), List.of(), List.of());

  public SearchQuery {
    nameTerms = normalisedCopy(nameTerms);
    tagTerms = normalisedCopy(tagTerms);
    modIdTerms = normalisedCopy(modIdTerms);
  }

  private static List<String> normalisedCopy(List<String> terms) {
    if (terms == null || terms.isEmpty()) {
      return List.of();
    }
    final List<String> normalised = new ArrayList<>(terms.size());
    for (final String term : terms) {
      final String candidate = FilterText.normalise(term);
      if (!candidate.isEmpty()) {
        normalised.add(candidate);
      }
    }
    return List.copyOf(normalised);
  }

  public boolean isEmpty() {
    return nameTerms.isEmpty() && tagTerms.isEmpty() && modIdTerms.isEmpty();
  }
}
