package com.grahambartley.toomanychests.screen;

/**
 * What one click on the item grid means.
 *
 * <p>Naming the meaning rather than acting on it is what makes the gesture table testable. The grid
 * is not made of slots, so none of vanilla's click handling applies to it and every button and
 * modifier has to be decided here; a table this size decided inline in a mouse handler is a table
 * nobody can check against the design.
 */
public enum GridAction {
  /** Left click: as much as one stack will hold, onto the pointer. */
  WITHDRAW_STACK_TO_CURSOR,
  /** Right click: half a stack, onto the pointer, as vanilla splits a stack. */
  WITHDRAW_HALF_TO_CURSOR,
  /** Shift and left click: a stack straight into the player's own slots. */
  WITHDRAW_STACK_TO_INVENTORY,
  /** Middle click: ask how much, since no gesture can express an arbitrary number. */
  PROMPT_FOR_AMOUNT,
  /** Left click while holding something: give the network the whole stack. */
  DEPOSIT_CURSOR_STACK,
  /** Right click while holding something: give the network one of it. */
  DEPOSIT_ONE_FROM_CURSOR,
  /** The click meant nothing here. */
  NONE
}
