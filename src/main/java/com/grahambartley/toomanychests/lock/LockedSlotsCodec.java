package com.grahambartley.toomanychests.lock;

import com.mojang.serialization.Codec;

/**
 * Persists a locked-slot set as an ascending list of indices.
 *
 * <p>Decoding routes through {@link LockedSlots#of}, so a save edited by hand or written by an
 * older build cannot reintroduce an index the rest of the mod treats as impossible.
 */
public final class LockedSlotsCodec {
  public static final Codec<LockedSlots> CODEC =
      Codec.INT.listOf().xmap(LockedSlots::of, LockedSlots::sortedIndices);

  private LockedSlotsCodec() {}
}
