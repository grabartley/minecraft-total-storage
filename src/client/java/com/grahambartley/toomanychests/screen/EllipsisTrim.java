package com.grahambartley.toomanychests.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.StringVisitable;

public final class EllipsisTrim {
  public static final String ELLIPSIS = "...";

  public interface TextRoom {
    int widthOf(StringVisitable text);

    StringVisitable trimmedTo(StringVisitable text, int width);

    List<StringVisitable> wrapped(StringVisitable text, int width);
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

  public static List<StringVisitable> toLines(
      StringVisitable text, int width, int maxLines, TextRoom room) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(room, "room");
    if (width <= 0 || maxLines <= 0) {
      return List.of();
    }
    final List<StringVisitable> wrapped = room.wrapped(text, width);
    if (wrapped.size() <= maxLines) {
      return List.copyOf(wrapped);
    }
    final List<StringVisitable> kept = new ArrayList<>(wrapped.subList(0, maxLines - 1));
    final StringVisitable overflow =
        StringVisitable.concat(wrapped.subList(maxLines - 1, wrapped.size()));
    kept.add(toWidth(overflow, width, room));
    return List.copyOf(kept);
  }
}
