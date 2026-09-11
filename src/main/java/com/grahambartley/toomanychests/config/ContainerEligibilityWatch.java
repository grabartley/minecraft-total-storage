package com.grahambartley.toomanychests.config;

import java.util.Map;

public final class ContainerEligibilityWatch {
  private Map<String, Boolean> seen = Map.of();

  public boolean moved(TooManyChestsConfig config) {
    if (config == null) {
      return false;
    }
    final Map<String, Boolean> overrides = config.containerEligibilityOverrides();
    if (overrides.equals(seen)) {
      return false;
    }
    seen = overrides;
    return true;
  }
}
