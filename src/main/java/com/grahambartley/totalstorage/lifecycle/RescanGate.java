package com.grahambartley.totalstorage.lifecycle;

public final class RescanGate {
  private boolean claimed;
  private int claimedTick;

  public boolean claim(int tick) {
    if (claimed && claimedTick == tick) {
      return false;
    }
    claimed = true;
    claimedTick = tick;
    return true;
  }

  public void reset() {
    claimed = false;
  }
}
