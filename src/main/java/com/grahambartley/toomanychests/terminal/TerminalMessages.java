package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.tag.TagNameOutcome;
import com.grahambartley.toomanychests.tag.TagNameRule;
import com.grahambartley.toomanychests.text.CountedNoun;
import net.minecraft.text.Text;

public final class TerminalMessages {
  private static final String PREFIX = "block.too-many-chests.terminal.";

  private TerminalMessages() {}

  public static Text unpowered() {
    return line("unpowered", "This terminal needs a redstone signal before it will open");
  }

  public static Text accessDenied() {
    return line("denied", "This terminal belongs to someone else");
  }

  public static Text accessRevoked() {
    return line("revoked", "Your access to that terminal has been taken away");
  }

  public static Text notYours() {
    return line("not-yours", "That terminal is not yours to configure");
  }

  public static Text containerNotInNetwork() {
    return line("container-out-of-range", "This terminal does not reach that container");
  }

  public static Text radiusClamped(int blocks) {
    return Text.translatableWithFallback(
        PREFIX + "radius-clamped",
        "The server caps this terminal at %s",
        CountedNoun.BLOCKS.of(blocks));
  }

  public static Text rescanned(int containers) {
    return Text.translatableWithFallback(
        PREFIX + "rescanned",
        "Scanned the region again and found %s",
        CountedNoun.CONTAINERS.of(containers));
  }

  public static Text tagNameRejected(TagNameOutcome why) {
    return switch (why) {
      case BLANK -> line("tag-blank", "A tag needs a name");
      case TOO_LONG ->
          Text.translatableWithFallback(
              PREFIX + "tag-too-long",
              "A tag name may be at most %s",
              CountedNoun.CHARACTERS.of(TagNameRule.MAX_LENGTH));
      case ILLEGAL_CHARACTER ->
          line("tag-illegal", "A tag name takes letters, digits, spaces, dashes and underscores");
      case DUPLICATE -> line("tag-duplicate", "This network already has a tag by that name");
      case PRESET_COLLISION -> line("tag-preset", "That name belongs to a preset tag");
      case OK -> line("tag-blank", "A tag needs a name");
    };
  }

  public static Text tagLimitReached(int limit) {
    return Text.translatableWithFallback(
        PREFIX + "tag-limit",
        "This network already holds its %s",
        CountedNoun.CUSTOM_TAGS.of(limit));
  }

  public static Text tagUnknown() {
    return line("tag-unknown", "That tag is not on this network");
  }

  public static Text tagIsPreset() {
    return line("tag-immutable", "Preset tags are worked out from the item, not assigned");
  }

  public static Text tagAssignedTo(int items) {
    return Text.translatableWithFallback(
        PREFIX + "tag-assigned", "Tagged %s", CountedNoun.ITEMS.of(items));
  }

  public static Text tagDeleted(int items) {
    return Text.translatableWithFallback(
        PREFIX + "tag-deleted",
        "Deleted the tag and cleared it from %s",
        CountedNoun.ITEMS.of(items));
  }

  public static Text tagsCopied(int imported, int skipped) {
    return skipped == 0
        ? Text.translatableWithFallback(
            PREFIX + "tags-copied", "Copied %s across", CountedNoun.TAGS.of(imported))
        : Text.translatableWithFallback(
            PREFIX + "tags-copied-partial",
            "Copied %s across and skipped %s",
            CountedNoun.TAGS.of(imported),
            skipped);
  }

  public static Text tagsCopiedNothing() {
    return line("tags-copied-none", "That terminal had nothing this one was missing");
  }

  private static Text line(String key, String english) {
    return Text.translatableWithFallback(PREFIX + key, english);
  }
}
