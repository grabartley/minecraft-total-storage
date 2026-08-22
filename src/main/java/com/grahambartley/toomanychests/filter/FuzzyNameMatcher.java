package com.grahambartley.toomanychests.filter;

/**
 * Decides whether a search term matches a candidate string, from an exact hit down to a
 * subsequence, so {@code dch} finds Dark Oak Chest.
 *
 * <p>Acceptance is deliberately separate from ranking. This answers only whether a row belongs in
 * the results at all; the terminal grid decides what order to present them in.
 */
public final class FuzzyNameMatcher {
  private FuzzyNameMatcher() {}

  /**
   * @param candidate the text being searched, such as a display name or an item identifier
   * @param term the normalised term the player typed
   * @return true when every character of the term appears in the candidate, in order
   */
  public static boolean matches(String candidate, String term) {
    final String normalisedTerm = FilterText.normalise(term);
    if (normalisedTerm.isEmpty()) {
      return true;
    }
    final String normalisedCandidate = FilterText.normalise(candidate);
    if (normalisedCandidate.isEmpty()) {
      return false;
    }
    return isSubsequence(normalisedCandidate, normalisedTerm);
  }

  private static boolean isSubsequence(String candidate, String term) {
    int termIndex = 0;
    for (int i = 0; i < candidate.length() && termIndex < term.length(); i++) {
      if (candidate.charAt(i) == term.charAt(termIndex)) {
        termIndex++;
      }
    }
    return termIndex == term.length();
  }
}
