package com.grahambartley.toomanychests.operation;

/**
 * The vanilla facts about stack sizes that the wire has to be bounded against.
 *
 * <p>Held apart from any one operation so a deposit's bound does not depend on a withdrawal's. Both
 * are bounding the same thing, which is how much of an item could possibly be in a player's hands.
 */
public final class ItemStackLimits {
  /** The largest stack size any item can be given, which is vanilla's own ceiling. */
  public static final int MAX_STACK_SIZE = 99;

  private ItemStackLimits() {}
}
