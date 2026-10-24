package com.grahambartley.totalstorage.tag;

public enum TagNameOutcome {
  OK,

  BLANK,

  TOO_LONG,

  ILLEGAL_CHARACTER,

  DUPLICATE,

  PRESET_COLLISION;

  public boolean isAccepted() {
    return this == OK;
  }
}
