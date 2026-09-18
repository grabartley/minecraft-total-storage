package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.tag.TagSkipReason;
import com.grahambartley.toomanychests.tag.TagSkips;
import com.grahambartley.toomanychests.text.CountedNoun;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class TagSetMessages {
  private static final String PREFIX = "command.too-many-chests.terminal.tags.";

  private TagSetMessages() {}

  public static Text help() {
    return line(
        "help",
        "/tmc terminal <pos> tags export <file> | import <file> <merge|replace> | copy <pos>"
            + " <merge|replace> moves a custom tag set between networks");
  }

  public static Text exported(int tags, String file) {
    return line("exported", "Exported %s to %s", CountedNoun.CUSTOM_TAGS.of(tags), file);
  }

  public static Text exportFailed() {
    return line("export.failed", "Could not write that tag set, see the server log for why");
  }

  public static Text fileUnreadable(String file) {
    return line("file.unreadable", "There is no readable tag set called %s", file);
  }

  public static Text fileNameRejected(String file) {
    return line(
        "file.rejected",
        "'%s' is not a tag set name. Use letters, digits, dashes and underscores",
        file);
  }

  public static Text imported(int imported, TagSkips skips) {
    return skips.isEmpty()
        ? line("imported", "Brought %s across", CountedNoun.TAGS.of(imported))
        : line(
            "imported.partial",
            "Brought %s across and skipped %s: %s",
            CountedNoun.TAGS.of(imported),
            skips.total(),
            reasons(skips));
  }

  public static Text importedNothing(TagSkips skips) {
    return skips.isEmpty()
        ? line("imported.none", "That tag set was already there")
        : line(
            "imported.all-skipped",
            "Skipped every one of the %s: %s",
            CountedNoun.TAGS.of(skips.total()),
            reasons(skips));
  }

  private static Text reasons(TagSkips skips) {
    final MutableText named = Text.empty();
    boolean first = true;
    for (final TagSkipReason reason : TagSkipReason.values()) {
      final int count = skips.countOf(reason);
      if (count == 0) {
        continue;
      }
      if (!first) {
        named.append(", ");
      }
      named.append(reason(reason, count));
      first = false;
    }
    return named;
  }

  private static Text reason(TagSkipReason reason, int count) {
    return Text.translatableWithFallback(
        PREFIX + "skipped." + reason.id(), "%s " + english(reason), count);
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

  public static Text transferRefused() {
    return line("refused", "Both terminals have to be yours before a tag set can move");
  }

  public static Text unknownMode(String requested) {
    return line("mode.unknown", "'%s' is not a mode. Modes are: merge, replace", requested);
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
