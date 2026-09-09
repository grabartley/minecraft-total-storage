package com.grahambartley.toomanychests.tag;

public enum TagEditOutcome {
  CHANGED,

  UNCHANGED,

  DENIED,

  NO_TERMINAL,

  UNKNOWN_TAG,

  PRESET_IMMUTABLE,

  TOO_MANY_TAGS,

  NAME_REJECTED;

  public boolean isChange() {
    return this == CHANGED;
  }
}
