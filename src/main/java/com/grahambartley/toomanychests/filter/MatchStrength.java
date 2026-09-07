package com.grahambartley.toomanychests.filter;

/**
 * How well one piece of text answers one search term, from the whole string down to a subsequence.
 *
 * <p>One implementation serves both halves of a search. Acceptance asks only whether the strength
 * is a match at all, and ranking reads how strong it was, so a row can never be accepted by one
 * rule and ordered by another that disagrees with it.
 *
 * <p>Declared strongest first, so the natural order of the enum is the order results are shown in.
 */
public enum MatchStrength {
  /** The candidate is the term, which is as close as an answer gets. */
  EXACT,

  /** The candidate starts with the term, which is what a player half way through typing means. */
  PREFIX,

  /** The term appears somewhere in the candidate, whole and unbroken. */
  SUBSTRING,

  /**
   * Every character of the term appears in order but not together, so {@code dch} finds a chest.
   */
  FUZZY,

  /** The term is not in the candidate at all. */
  NONE;

  public boolean isMatch() {
    return this != NONE;
  }

  /** The stronger of two strengths, which is what a term matching a name and a path scores. */
  public static MatchStrength best(MatchStrength first, MatchStrength second) {
    return first.compareTo(second) <= 0 ? first : second;
  }

  /**
   * How the candidate answers the term, fuzzily when it does not answer it literally.
   *
   * <p>An empty term is answered exactly by anything, because a query that narrows nothing puts
   * every row on an equal footing rather than rejecting them all.
   */
  public static MatchStrength of(String candidate, String term) {
    final String needle = FilterText.normalise(term);
    final String haystack = FilterText.normalise(candidate);
    final MatchStrength literal = literalStrength(haystack, needle);
    if (literal.isMatch()) {
      return literal;
    }
    return isSubsequence(haystack, needle) ? FUZZY : NONE;
  }

  /**
   * How the candidate answers the term, counting only unbroken matches.
   *
   * <p>Per ADR 0013 a term carrying a namespace is matched literally against the whole identifier,
   * so this stops short of a subsequence rather than letting {@code mine:c} reach every item the
   * {@code minecraft} namespace holds.
   */
  public static MatchStrength literal(String candidate, String term) {
    return literalStrength(FilterText.normalise(candidate), FilterText.normalise(term));
  }

  private static MatchStrength literalStrength(String haystack, String needle) {
    if (needle.isEmpty()) {
      return EXACT;
    }
    if (haystack.isEmpty()) {
      return NONE;
    }
    if (haystack.equals(needle)) {
      return EXACT;
    }
    if (haystack.startsWith(needle)) {
      return PREFIX;
    }
    return haystack.contains(needle) ? SUBSTRING : NONE;
  }

  private static boolean isSubsequence(String haystack, String needle) {
    int needleIndex = 0;
    for (int i = 0; i < haystack.length() && needleIndex < needle.length(); i++) {
      if (haystack.charAt(i) == needle.charAt(needleIndex)) {
        needleIndex++;
      }
    }
    return needleIndex == needle.length();
  }
}
