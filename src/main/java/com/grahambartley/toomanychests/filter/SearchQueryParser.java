package com.grahambartley.toomanychests.filter;

import java.util.ArrayList;
import java.util.List;

public final class SearchQueryParser {
  private static final char TAG_PREFIX = '#';
  private static final char MOD_ID_PREFIX = '@';

  private SearchQueryParser() {}

  public static SearchQuery parse(String raw) {
    if (FilterText.isBlank(raw)) {
      return SearchQuery.EMPTY;
    }
    final List<String> nameTerms = new ArrayList<>();
    final List<String> tagTerms = new ArrayList<>();
    final List<String> modIdTerms = new ArrayList<>();
    for (final String token : raw.trim().split("\\s+")) {
      if (token.isEmpty()) {
        continue;
      }
      switch (token.charAt(0)) {
        case TAG_PREFIX -> tagTerms.add(token.substring(1));
        case MOD_ID_PREFIX -> modIdTerms.add(token.substring(1));
        default -> nameTerms.add(token);
      }
    }
    return new SearchQuery(nameTerms, tagTerms, modIdTerms);
  }
}
