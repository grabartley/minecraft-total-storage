package com.grahambartley.toomanychests.screen;

import org.lwjgl.glfw.GLFW;

public final class GridClick {
  public static final int LEFT_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT;
  public static final int RIGHT_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
  public static final int MIDDLE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

  private GridClick() {}

  public static GridAction of(
      int button, boolean shiftDown, boolean cursorHoldsStack, boolean cellHoldsRow) {
    if (cursorHoldsStack) {
      return switch (button) {
        case LEFT_BUTTON -> GridAction.DEPOSIT_CURSOR_STACK;
        case RIGHT_BUTTON -> GridAction.DEPOSIT_ONE_FROM_CURSOR;
        default -> GridAction.NONE;
      };
    }
    if (!cellHoldsRow) {
      return GridAction.NONE;
    }
    return switch (button) {
      case LEFT_BUTTON ->
          shiftDown ? GridAction.WITHDRAW_STACK_TO_INVENTORY : GridAction.WITHDRAW_STACK_TO_CURSOR;
      case RIGHT_BUTTON -> GridAction.WITHDRAW_HALF_TO_CURSOR;
      case MIDDLE_BUTTON -> GridAction.PROMPT_FOR_AMOUNT;
      default -> GridAction.NONE;
    };
  }
}
