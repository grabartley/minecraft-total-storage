package com.grahambartley.toomanychests.screen;

import java.util.Objects;
import net.minecraft.text.StringVisitable;

public final class EllipsisTrim {
  public static final String ELLIPSIS = "...";

  public interface TextRoom {
    int widthOf(StringVisitable text);

    StringVisitable trimmedTo(StringVisitable text, int width);
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
}
