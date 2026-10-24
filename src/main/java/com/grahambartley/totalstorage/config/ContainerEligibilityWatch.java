package com.grahambartley.totalstorage.config;

import java.util.Map;

public final class ContainerEligibilityWatch {
  private Map<String, Boolean> seen = Map.of();

  public void seed(TotalStorageConfig config) {
    seen = config.containerEligibilityOverrides();
  }

  public boolean moved(TotalStorageConfig config) {
    final Map<String, Boolean> overrides = config.containerEligibilityOverrides();
    if (overrides.equals(seen)) {
      return false;
    }
    seen = overrides;
    return true;
  }
}
