package com.grahambartley.totalstorage.lock;

import com.mojang.serialization.Codec;

public final class LockedSlotsCodec {
  public static final Codec<LockedSlots> CODEC =
      Codec.INT.listOf().xmap(LockedSlots::of, LockedSlots::sortedIndices);

  private LockedSlotsCodec() {}
}
