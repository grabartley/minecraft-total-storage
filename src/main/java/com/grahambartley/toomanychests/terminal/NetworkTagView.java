package com.grahambartley.toomanychests.terminal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public record NetworkTagView(
    boolean answered,
    boolean mayAdminister,
    List<NetworkTagEntry> tags,
    Optional<ItemVariant> subject,
    Set<String> subjectTags) {

  public static final int MAX_TAGS = 128;

  private static final NetworkTagView PENDING =
      new NetworkTagView(false, false, List.of(), Optional.empty(), Set.of());

  public NetworkTagView(
      boolean mayAdminister,
      List<NetworkTagEntry> tags,
      Optional<ItemVariant> subject,
      Set<String> subjectTags) {
    this(true, mayAdminister, tags, subject, subjectTags);
  }

  public NetworkTagView {
    tags = tags == null ? List.of() : List.copyOf(tags);
    subject = subject == null ? Optional.empty() : subject;
    subjectTags = subjectTags == null ? Set.of() : Set.copyOf(subjectTags);
    if (tags.size() > MAX_TAGS) {
      throw new IllegalArgumentException("a tag view may not carry " + tags.size() + " tags");
    }
  }

  public static NetworkTagView pending() {
    return PENDING;
  }

  public List<NetworkTagEntry> custom() {
    return tags.stream().filter(tag -> !tag.immutable()).toList();
  }

  public boolean subjectCarries(String tagId) {
    return subjectTags.contains(tagId);
  }
}
