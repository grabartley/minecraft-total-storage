package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.ServerConfigService;
import java.util.Comparator;

public final class ConfiguredSortOrder {
  private ConfiguredSortOrder() {}

  public static Comparator<SortCandidate> current() {
    return SortComparator.forOrdering(ServerConfigService.current().sortOrdering());
  }
}
