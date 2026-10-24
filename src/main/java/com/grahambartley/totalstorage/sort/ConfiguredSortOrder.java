package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.config.ServerConfigService;
import java.util.Comparator;

public final class ConfiguredSortOrder {
  private ConfiguredSortOrder() {}

  public static Comparator<SortCandidate> current() {
    return SortComparator.forOrdering(ServerConfigService.current().sortOrdering());
  }
}
