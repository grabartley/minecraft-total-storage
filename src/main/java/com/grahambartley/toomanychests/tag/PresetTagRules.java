package com.grahambartley.toomanychests.tag;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Turns the raw preset matches for an item into the set it is filed under, as a pure decision over
 * plain data. An item may hold several presets at once; matching none means the fallback rather
 * than nothing at all, which is what guarantees every item is reachable from the filter column.
 */
public final class PresetTagRules {
  private PresetTagRules() {}

  /**
   * @param matched every preset whose membership the item satisfied, in any order
   * @return the presets to file the item under, in filter column order
   */
  public static Set<PresetTag> assign(Collection<PresetTag> matched) {
    EnumSet<PresetTag> assigned = EnumSet.noneOf(PresetTag.class);
    for (PresetTag preset : matched) {
      if (preset != null && preset != PresetTag.FALLBACK) {
        assigned.add(preset);
      }
    }
    if (assigned.isEmpty()) {
      assigned.add(PresetTag.FALLBACK);
    }
    return Collections.unmodifiableSet(assigned);
  }
}
