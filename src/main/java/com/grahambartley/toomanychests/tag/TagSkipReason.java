package com.grahambartley.toomanychests.tag;

public enum TagSkipReason {
  MALFORMED("malformed"),

  PRESET_NAME("preset-name"),

  TOO_LONG("too-long"),

  ILLEGAL_NAME("illegal-name"),

  NO_ROOM("no-room");

  private final String id;

  TagSkipReason(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static TagSkipReason of(TagNameOutcome outcome) {
    return switch (outcome) {
      case TOO_LONG -> TOO_LONG;
      case PRESET_COLLISION -> PRESET_NAME;
      case BLANK -> MALFORMED;
      case ILLEGAL_CHARACTER -> ILLEGAL_NAME;
      case OK, DUPLICATE ->
          throw new IllegalArgumentException(outcome + " is not a reason an entry was skipped");
    };
  }
}
