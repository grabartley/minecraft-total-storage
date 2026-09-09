package com.grahambartley.toomanychests.screenhandler;

public enum TagRowRegion {
  NAME,

  DELETE,

  NONE;

  public boolean isSomething() {
    return this != NONE;
  }
}
