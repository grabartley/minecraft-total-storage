package com.grahambartley.totalstorage.screen;

import java.util.List;
import java.util.Objects;
import net.minecraft.text.Text;

public record HintLine(List<HintLine.Part> parts) {
  public sealed interface Part permits Key, Words, LockBadge {}

  public record Key(Text label) implements Part {
    public Key {
      Objects.requireNonNull(label, "label");
    }
  }

  public record Words(Text text) implements Part {
    public Words {
      Objects.requireNonNull(text, "text");
    }
  }

  public record LockBadge() implements Part {}

  public HintLine {
    parts = List.copyOf(parts);
  }

  public static HintLine of(Part... parts) {
    return new HintLine(List.of(parts));
  }

  public static Part key(String name) {
    return new Key(TerminalScreenText.of("key." + name));
  }

  public static Part words(Text text) {
    return new Words(text);
  }

  public static Part words(String suffix, Object... arguments) {
    return new Words(TerminalScreenText.of(suffix, arguments));
  }

  public static Part badge() {
    return new LockBadge();
  }
}
