package com.grahambartley.totalstorage.lifecycle;

import java.util.OptionalInt;

public final class RescanGate {
  private OptionalInt claimedTick = OptionalInt.empty();

  public boolean claim(int tick) {
    if (claimedTick.isPresent() && claimedTick.getAsInt() == tick) {
      return false;
    }
    claimedTick = OptionalInt.of(tick);
    return true;
  }

  public void reset() {
    claimedTick = OptionalInt.empty();
  }
}
