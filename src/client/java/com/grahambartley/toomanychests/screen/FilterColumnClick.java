package com.grahambartley.toomanychests.screen;

public enum FilterColumnClick {
  FILTER_CHANGED,

  SCROLLBAR_GRABBED,

  HANDLED,

  IGNORED;

  public boolean isHandled() {
    return this != IGNORED;
  }

  public boolean changedFilter() {
    return this == FILTER_CHANGED;
  }
}
