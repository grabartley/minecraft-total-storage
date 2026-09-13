package com.grahambartley.toomanychests.tag;

import java.util.List;

public final class TagSetImport {
  private TagSetImport() {}

  public static TagSetImportResult into(
      NetworkTagSet target, NetworkTagSet source, TagSetImportMode mode) {
    if (target == null || source == null || mode == null) {
      return new TagSetImportResult(target == null ? NetworkTagSet.defaults() : target, 0, 0);
    }
    NetworkTagSet running = mode.clearsWhatIsThere() ? new NetworkTagSet(List.of()) : target;
    int imported = 0;
    int skipped = 0;
    for (final NetworkTag tag : source.custom()) {
      if (running.contains(tag.id())) {
        running = running.withItems(tag.id(), tag.items());
        imported++;
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
