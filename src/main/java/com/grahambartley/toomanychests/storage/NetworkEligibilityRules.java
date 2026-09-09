package com.grahambartley.toomanychests.storage;

import java.util.Optional;

public final class NetworkEligibilityRules {
  private NetworkEligibilityRules() {}

  public static boolean isEligible(
      boolean hasItemStorage, boolean deniedByDefault, Optional<Boolean> operatorOverride) {
    if (!hasItemStorage) {
      return false;
    }
    return operatorOverride.orElse(!deniedByDefault);
  }
}
