package com.grahambartley.toomanychests.filter;

/**
 * Decides whether a search term matches a candidate string, from an exact hit down to a
 * subsequence, so {@code dch} finds Dark Oak Chest.
 *
 * <p>Acceptance is deliberately separate from ranking. This answers only whether a row belongs in
 * the results at all; how close it came is {@link MatchStrength}, which this reads so the two can
 * never disagree about what a match is.
 */
public final class FuzzyNameMatcher {
  private FuzzyNameMatcher() {}

  /**
   * @param candidate the text being searched, such as a display name or an item identifier
   * @param term the normalised term the player typed
   * @return true when every character of the term appears in the candidate, in order
   */
  public static boolean matches(String candidate, String term) {
    return MatchStrength.of(candidate, term).isMatch();
  }
}
