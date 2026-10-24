package com.grahambartley.totalstorage.tag;

import java.util.List;
import java.util.Objects;

public final class TagSetImport {
  private TagSetImport() {}

  public static TagSetImportResult into(
      NetworkTagSet target, NetworkTagSet source, TagSetImportMode mode) {
    return into(target, source == null ? null : TagSetContents.of(source), mode);
  }

  public static TagSetImportResult into(
      NetworkTagSet target, TagSetContents source, TagSetImportMode mode) {
    Objects.requireNonNull(target, "target");
    if (source == null || mode == null) {
      return new TagSetImportResult(target, 0, TagSkips.NONE);
    }
    NetworkTagSet running = mode.clearsWhatIsThere() ? new NetworkTagSet(List.of()) : target;
    int imported = 0;
    TagSkips skips = TagSkips.of(TagSkipReason.MALFORMED, source.malformed());
    for (final NetworkTag tag : source.tags()) {
      final TagNameOutcome name = TagNameRule.check(tag.id(), running);
      if (name == TagNameOutcome.DUPLICATE) {
        final NetworkTagSet grown = running.withItems(tag.id(), tag.items());
        if (!grown.equals(running)) {
          imported++;
        }
        running = grown;
      } else if (!name.isAccepted()) {
        skips = skips.and(TagSkipReason.of(name));
      } else if (running.custom().size() >= NetworkTagSet.MAX_CUSTOM) {
        skips = skips.and(TagSkipReason.NO_ROOM);
      } else {
        running = running.withTag(NetworkTag.custom(tag.id(), tag.displayName(), tag.items()));
        imported++;
      }
    }
    return new TagSetImportResult(running, imported, skips);
  }
}
