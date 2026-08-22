package com.grahambartley.toomanychests.storage;

import java.util.Optional;

/**
 * Whether a container may join a storage network at all, as a pure decision over the three inputs
 * that matter. Eligibility answers "may this join"; the role a network gives it is a separate
 * concern.
 */
public final class NetworkEligibilityRules {
  private NetworkEligibilityRules() {}

  /**
   * @param hasItemStorage whether anything at the position exposes item storage; nothing else can
   *     join a network regardless of what an operator asks for
   * @param deniedByDefault whether the block carries the ineligible tag
   * @param operatorOverride an operator's explicit choice for this block, if they made one
   */
  public static boolean isEligible(
      boolean hasItemStorage, boolean deniedByDefault, Optional<Boolean> operatorOverride) {
    if (!hasItemStorage) {
      return false;
    }
    return operatorOverride.orElse(!deniedByDefault);
  }
}
