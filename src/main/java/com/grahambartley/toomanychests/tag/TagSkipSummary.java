package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class TagSkipSummary {
  private static final String PREFIX =
      "command." + TooManyChests.MOD_ID + ".terminal.tags.skipped.";

  private static final String SEPARATOR = ", ";

  private TagSkipSummary() {}

  public static Text of(TagSkips skips) {
    final MutableText named = Text.empty();
    boolean first = true;
    for (final TagSkipReason reason : TagSkipReason.values()) {
      final int count = skips.countOf(reason);
      if (count == 0) {
        continue;
      }
      if (!first) {
        named.append(SEPARATOR);
      }
      named.append(reasonOf(reason, count));
      first = false;
    }
    return named;
  }

  private static Text reasonOf(TagSkipReason reason, int count) {
    return Text.translatableWithFallback(PREFIX + reason.id(), "%s " + english(reason), count);
  }

  private static String english(TagSkipReason reason) {
    return switch (reason) {
      case MALFORMED -> "malformed";
      case PRESET_NAME -> "preset name";
      case TOO_LONG -> "too long";
      case ILLEGAL_NAME -> "illegal name";
      case NO_ROOM -> "over the limit";
    };
  }
}
