package com.grahambartley.toomanychests.screen;

import org.lwjgl.glfw.GLFW;

/**
 * Turns a click on the item grid into what the player meant by it.
 *
 * <p>A cursor holding something changes what every button means, which is the rule vanilla
 * containers follow and the one players already have in their hands: a full cursor puts down, an
 * empty one picks up. Following it here is what lets the grid feel like a container without being
 * one.
 *
 * <p>Depositing is offered over the grid rather than only from the inventory below, because the
 * grid is where a player is looking and dropping a stack onto the network is the obvious gesture
 * there. It does not depend on the cell under the pointer: a network has no slot to drop into, so
 * where over the grid the click landed means nothing.
 */
public final class GridClick {
  public static final int LEFT_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT;
  public static final int RIGHT_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
  public static final int MIDDLE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

  private GridClick() {}

  /**
   * @param cursorHoldsStack whether the player is carrying something on the pointer
   * @param cellHoldsRow whether the cell under the pointer is showing an item
   */
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
