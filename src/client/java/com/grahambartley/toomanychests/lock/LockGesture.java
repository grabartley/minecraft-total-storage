package com.grahambartley.toomanychests.lock;

public final class LockGesture {
  public static final int LEFT_BUTTON = 0;

  private LockGesture() {}

  public static boolean isLockToggle(int button, boolean altDown) {
    return altDown && button == LEFT_BUTTON;
  }
}
