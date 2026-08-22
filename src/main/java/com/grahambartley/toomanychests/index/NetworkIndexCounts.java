package com.grahambartley.toomanychests.index;

/** Count arithmetic for the index, where a total may span any number of containers. */
final class NetworkIndexCounts {
  private NetworkIndexCounts() {}

  /**
   * Saturates rather than overflowing. A modded storage is free to report an amount near {@code
   * Long.MAX_VALUE}, and a wrapped total would render as a negative stack count instead of a big
   * one.
   */
  static long saturatedAdd(long left, long right) {
    final long sum = left + right;
    return ((left ^ sum) & (right ^ sum)) < 0L ? Long.MAX_VALUE : sum;
  }
}
