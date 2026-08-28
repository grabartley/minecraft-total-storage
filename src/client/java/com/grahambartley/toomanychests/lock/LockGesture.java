package com.grahambartley.toomanychests.lock;

/**
 * The click that means "lock this slot" rather than "move this item".
 *
 * <p>Alt with the left button is the one combination the inventory screen leaves free. Shift,
 * control, double click, drag, and the number keys are all spoken for by vanilla, and a gesture
 * that shared any of them would move a stack every time it was misread.
 */
public final class LockGesture {
  public static final int LEFT_BUTTON = 0;

  private LockGesture() {}

  public static boolean isLockToggle(int button, boolean altDown) {
    return altDown && button == LEFT_BUTTON;
  }
}
