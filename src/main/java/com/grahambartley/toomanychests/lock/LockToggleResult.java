package com.grahambartley.toomanychests.lock;

/** The outcome of a lock toggle together with the set it produced. */
public record LockToggleResult(LockToggleOutcome outcome, LockedSlots locks) {}
