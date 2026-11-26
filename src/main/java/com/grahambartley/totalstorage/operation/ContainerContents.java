package com.grahambartley.totalstorage.operation;

public enum ContainerContents {
  HOLDS_ITEM(0),
  EMPTY(1),
  OTHER_ITEMS(2);

  private final int preference;

  ContainerContents(int preference) {
    this.preference = preference;
  }

  public int preference() {
    return preference;
  }
}
