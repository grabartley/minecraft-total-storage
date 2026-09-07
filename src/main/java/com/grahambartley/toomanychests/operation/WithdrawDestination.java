package com.grahambartley.toomanychests.operation;

/**
 * Where a withdrawal puts what it takes.
 *
 * <p>The two differ by how much they can hold, which is what bounds an amount before anything is
 * extracted. A cursor is one stack whatever the player asks for; the inventory is as many stacks as
 * it has slots, which is what a custom amount larger than a stack needs.
 *
 * <p>The inventory's bound counts every slot rather than only the unlocked ones. It is a ceiling
 * rather than a promise, and erring high is the safe direction: locks are enforced where the items
 * actually move, and a bound that shrank as a player pinned slots would refuse withdrawals it had
 * no business refusing.
 */
public enum WithdrawDestination {
  CURSOR,
  INVENTORY;

  public boolean holdsOneStackOnly() {
    return this == CURSOR;
  }
}
