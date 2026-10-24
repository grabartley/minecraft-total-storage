package com.grahambartley.totalstorage.lock;

public record LockToggleResult(LockToggleOutcome outcome, LockedSlots locks) {}
