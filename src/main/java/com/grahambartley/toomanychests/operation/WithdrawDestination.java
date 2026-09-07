package com.grahambartley.toomanychests.operation;

/**
 * Where a withdrawal puts what it takes.
 *
 * <p>The two differ by how much they can hold, which is what bounds an amount before anything is
 * extracted. A cursor is one stack whatever the player asks for; the inventory is as many stacks as
 * it has unlocked slots, which is what a custom amount larger than a stack needs.
 */
public enum WithdrawDestination {
  CURSOR,
  INVENTORY;

  public boolean holdsOneStackOnly() {
    return this == CURSOR;
  }
}
