package com.grahambartley.totalstorage.filter;

public enum MatchStrength {
  EXACT,

  PREFIX,

  SUBSTRING,

  FUZZY,

  NONE;

  public boolean isMatch() {
    return this != NONE;
  }

  public static MatchStrength best(MatchStrength first, MatchStrength second) {
    return first.compareTo(second) <= 0 ? first : second;
  }

  public static MatchStrength of(String candidate, String term) {
    final String needle = FilterText.normalise(term);
    final String haystack = FilterText.normalise(candidate);
    final MatchStrength literal = literalStrength(haystack, needle);
    if (literal.isMatch()) {
      return literal;
    }
    return isSubsequence(haystack, needle) ? FUZZY : NONE;
  }

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
