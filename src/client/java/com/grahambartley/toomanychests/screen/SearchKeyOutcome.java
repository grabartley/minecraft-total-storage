package com.grahambartley.toomanychests.screen;

public enum SearchKeyOutcome {
  CLEAR_SEARCH,

  CLOSE_SCREEN,

  TYPE,

  PASS_THROUGH;

  public static SearchKeyOutcome of(boolean escape, boolean focused, boolean searched) {
    if (escape) {
      return searched ? CLEAR_SEARCH : CLOSE_SCREEN;
    }
    return focused ? TYPE : PASS_THROUGH;
  }
}
