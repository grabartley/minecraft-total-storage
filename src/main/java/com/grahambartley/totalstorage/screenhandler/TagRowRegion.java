package com.grahambartley.totalstorage.screenhandler;

public enum TagRowRegion {
  NAME,

  DELETE,

  NONE;

  public boolean isSomething() {
    return this != NONE;
  }
}
