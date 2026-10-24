package com.grahambartley.totalstorage.tag;

import java.util.List;

public record TagSetContents(List<NetworkTag> tags, int malformed) {
  public TagSetContents {
    tags = tags == null ? List.of() : List.copyOf(tags);
    malformed = Math.max(0, malformed);
  }

  public static TagSetContents of(NetworkTagSet tags) {
    return new TagSetContents(tags == null ? List.of() : tags.custom(), 0);
  }
}
