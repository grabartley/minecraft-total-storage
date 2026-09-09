package com.grahambartley.toomanychests.index;

final class NetworkIndexCounts {
  private NetworkIndexCounts() {}

  static long saturatedAdd(long left, long right) {
    final long sum = left + right;
    return ((left ^ sum) & (right ^ sum)) < 0L ? Long.MAX_VALUE : sum;
  }
}
