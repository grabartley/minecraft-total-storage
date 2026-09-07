package com.grahambartley.toomanychests.screen;

/**
 * What a key press means to a terminal whose search box is taking the typing.
 *
 * <p>The box takes focus the moment the screen opens, so the keys a container screen normally acts
 * on are keys the player is trying to type. The inventory key would close the terminal instead of
 * writing an {@code e}, and a digit would swap a hotbar slot instead of writing a {@code 3}. While
 * the box has focus every key belongs to it.
 *
 * <p>Escape is the exception, and it does two things in turn: it empties the box, and only once
 * there is nothing left to empty does it close the screen. A query typed by accident then costs one
 * key to undo rather than a reopened terminal and a lost scroll position.
 */
public enum SearchKeyOutcome {
  /** Empty the box and stay open, which is what escape means while a query is showing. */
  CLEAR_SEARCH,

  /** Let the screen close, which is what escape means once the box is already empty. */
  CLOSE_SCREEN,

  /** The box takes the key, and the screen behind it does not see it. */
  TYPE,

  /** Nothing is being typed, so the key means whatever it usually means. */
  PASS_THROUGH;

  /**
   * @param escape whether the key was escape
   * @param focused whether the search box currently has focus
   * @param searched whether the box holds a query
   */
  public static SearchKeyOutcome of(boolean escape, boolean focused, boolean searched) {
    if (escape) {
      return searched ? CLEAR_SEARCH : CLOSE_SCREEN;
    }
    return focused ? TYPE : PASS_THROUGH;
  }
}
