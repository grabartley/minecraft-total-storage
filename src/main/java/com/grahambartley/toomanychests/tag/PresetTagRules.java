package com.grahambartley.toomanychests.tag;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class PresetTagRules {
  private PresetTagRules() {}

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
