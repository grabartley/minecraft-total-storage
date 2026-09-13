package com.grahambartley.toomanychests.tag;

import java.util.List;
import java.util.Objects;

public final class TagSetImport {
  private TagSetImport() {}

  public static TagSetImportResult into(
      NetworkTagSet target, NetworkTagSet source, TagSetImportMode mode) {
    Objects.requireNonNull(target, "target");
    if (source == null || mode == null) {
      return new TagSetImportResult(target, 0, 0);
    }
    NetworkTagSet running = mode.clearsWhatIsThere() ? new NetworkTagSet(List.of()) : target;
    int imported = 0;
    int skipped = 0;
    for (final NetworkTag tag : source.custom()) {
      if (running.contains(tag.id())) {
        final NetworkTagSet grown = running.withItems(tag.id(), tag.items());
        if (!grown.equals(running)) {
          imported++;
        }
        running = grown;
      } else if (hasRoomFor(running, tag)) {
        running = running.withTag(NetworkTag.custom(tag.id(), tag.displayName(), tag.items()));
        imported++;
      } else {
        skipped++;
      }
    }
    return new TagSetImportResult(running, imported, skipped);
  }

  private static boolean hasRoomFor(NetworkTagSet running, NetworkTag tag) {
    return running.custom().size() < NetworkTagSet.MAX_CUSTOM
        && TagNameRule.check(tag.id(), running).isAccepted();
  }
}
