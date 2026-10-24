package com.grahambartley.totalstorage.config;

public record ConfigRange(int min, int max) {
  public ConfigRange {
    if (min > max) {
      throw new IllegalArgumentException("A range cannot start above where it ends");
    }
  }

  public int clamp(int value) {
    return Math.max(min, Math.min(max, value));
  }

  public double fractionOf(int value) {
    return max == min ? 0.0d : (double) (clamp(value) - min) / (max - min);
  }

  public int valueAt(double fraction) {
    return clamp(min + (int) Math.round(fraction * (max - min)));
  }
}
