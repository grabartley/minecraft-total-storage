package com.grahambartley.totalstorage.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.StringVisitable;

public final class EllipsisTrim {
  public static final String ELLIPSIS = "...";

  public interface TextRoom {
    int widthOf(StringVisitable text);

    StringVisitable trimmedTo(StringVisitable text, int width);

    List<Integer> lineStarts(String text, int width);
  }

  private EllipsisTrim() {}

  public static StringVisitable toWidth(StringVisitable text, int width, TextRoom room) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(room, "room");
    if (width <= 0) {
      return StringVisitable.EMPTY;
    }
    if (room.widthOf(text) <= width) {
      return text;
    }
    final int kept = width - room.widthOf(StringVisitable.plain(ELLIPSIS));
    if (kept <= 0) {
      return StringVisitable.EMPTY;
    }
    return StringVisitable.concat(room.trimmedTo(text, kept), StringVisitable.plain(ELLIPSIS));
  }

  public static List<StringVisitable> toLines(String text, int width, int maxLines, TextRoom room) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(room, "room");
    if (width <= 0 || maxLines <= 0) {
      return List.of();
    }
    final List<Integer> starts = room.lineStarts(text, width);
    final int shown = Math.min(starts.size(), maxLines);
    final List<StringVisitable> lines = new ArrayList<>(shown);
    for (int line = 0; line < shown; line++) {
      final boolean last = line == shown - 1;
      final int end = last ? text.length() : starts.get(line + 1);
      final String piece = text.substring(starts.get(line), end).strip();
      lines.add(
          last && starts.size() > maxLines
              ? toWidth(StringVisitable.plain(piece), width, room)
              : StringVisitable.plain(piece));
    }
    return List.copyOf(lines);
  }
}
