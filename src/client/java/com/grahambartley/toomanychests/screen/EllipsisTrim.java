package com.grahambartley.toomanychests.screen;

import java.util.Objects;
import java.util.function.ToIntFunction;

public final class EllipsisTrim {
  public static final String ELLIPSIS = "...";

  private EllipsisTrim() {}

  public static String toWidth(String text, int width, ToIntFunction<String> measure) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(measure, "measure");
    if (width <= 0) {
      return "";
    }
    if (measure.applyAsInt(text) <= width) {
      return text;
    }
    final int room = width - measure.applyAsInt(ELLIPSIS);
    if (room <= 0) {
      return "";
    }
    int kept = 0;
    while (kept < text.length() && measure.applyAsInt(text.substring(0, kept + 1)) <= room) {
      kept++;
    }
    return kept == 0 ? "" : text.substring(0, kept).stripTrailing() + ELLIPSIS;
  }
}
