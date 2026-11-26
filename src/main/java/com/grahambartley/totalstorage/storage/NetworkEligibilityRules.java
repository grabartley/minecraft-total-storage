package com.grahambartley.totalstorage.storage;

import java.util.Optional;

public final class NetworkEligibilityRules {
  private NetworkEligibilityRules() {}

  public static boolean isEligible(
      boolean hasItemStorage, boolean deniedByDefault, Optional<Boolean> operatorOverride) {
    return hasItemStorage && allowsBlock(deniedByDefault, operatorOverride);
  }

  public static boolean allowsBlock(boolean deniedByDefault, Optional<Boolean> operatorOverride) {
    return operatorOverride.orElse(!deniedByDefault);
  }
}
