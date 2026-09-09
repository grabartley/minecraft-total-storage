package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.FilterText;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class FilterSelection {
  private final Set<String> active = new LinkedHashSet<>();

  private TagCombinationMode mode = TagCombinationMode.DEFAULT;

  public Set<String> active() {
    return Collections.unmodifiableSet(active);
  }

  public TagCombinationMode mode() {
    return mode;
  }

  public boolean isActive(String tagId) {
    return active.contains(FilterText.normalise(tagId));
  }

  public boolean isFiltering() {
    return !active.isEmpty();
  }

  public boolean toggle(String tagId) {
    final String id = FilterText.normalise(tagId);
    if (id.isEmpty()) {
      return false;
    }
    if (active.remove(id)) {
      return true;
    }
    if (active.size() >= NetworkTagSet.MAX_TOTAL) {
      return false;
    }
    active.add(id);
    return true;
  }

  public boolean clear() {
    if (active.isEmpty()) {
      return false;
    }
    active.clear();
    return true;
  }

  public void retainAll(Set<String> known) {
    active.retainAll(FilterText.normaliseAll(known));
  }

  public TagCombinationMode cycleMode() {
    mode = mode == TagCombinationMode.AND ? TagCombinationMode.OR : TagCombinationMode.AND;
    return mode;
  }
}
