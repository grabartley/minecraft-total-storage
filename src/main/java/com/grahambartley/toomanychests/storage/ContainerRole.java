package com.grahambartley.toomanychests.storage;

public enum ContainerRole {
  GENERAL,
  FILTERED,
  EXCLUDED;

  public static final ContainerRole DEFAULT = GENERAL;
}
