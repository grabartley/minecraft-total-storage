package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.tag.TagSkipSummary;
import com.grahambartley.totalstorage.tag.TagSkips;
import com.grahambartley.totalstorage.text.CountedNoun;
import net.minecraft.text.Text;

public final class TagSetMessages {
  private static final String PREFIX = "command.total-storage.terminal.tags.";

  private TagSetMessages() {}

  public static Text help() {
    return line(
        "help",
        "/ts terminal <pos> tags export <file> | import <file> <merge|replace> | copy <pos>"
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
            TagSkipSummary.of(skips));
  }

  public static Text importedNothing(TagSkips skips) {
    return skips.isEmpty()
        ? line("imported.none", "That tag set was already there")
        : line(
            "imported.all-skipped",
            "Skipped every one of the %s: %s",
            CountedNoun.TAGS.of(skips.total()),
            TagSkipSummary.of(skips));
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
