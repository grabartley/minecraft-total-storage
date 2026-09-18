package com.grahambartley.toomanychests.render;

import java.util.ArrayList;
import java.util.List;

public final class DashedEdge {
  public record Span(double start, double end) {}

  private DashedEdge() {}

  public static List<Span> of(double from, double to, int dashes) {
    if (dashes < 1) {
      throw new IllegalArgumentException("an edge cannot carry " + dashes + " dashes");
    }
    final double step = (to - from) / (2 * dashes - 1);
    final List<Span> spans = new ArrayList<>(dashes);
    for (int dash = 0; dash < dashes; dash++) {
      final double start = from + 2 * dash * step;
      spans.add(new Span(start, start + step));
    }
    return List.copyOf(spans);
  }
}
