package com.grahambartley.totalstorage.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkEligibilityRulesTest {
  @ParameterizedTest
  @CsvSource({
    "true,  false, ,      true",
    "true,  true,  ,      false",
    "false, false, ,      false",
    "false, true,  ,      false",
    "true,  false, true,  true",
    "true,  false, false, false",
    "true,  true,  true,  true",
    "true,  true,  false, false",
    "false, false, true,  false",
    "false, true,  true,  false",
  })
  void eligibilityFollowsStorageThenOverrideThenTag(
      boolean hasItemStorage, boolean deniedByDefault, Boolean override, boolean expected) {
    assertEquals(
        expected,
        NetworkEligibilityRules.isEligible(
            hasItemStorage, deniedByDefault, Optional.ofNullable(override)));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void nothingWithoutItemStorageCanJoinHoweverTheOperatorConfiguresIt(boolean override) {
    assertFalse(NetworkEligibilityRules.isEligible(false, false, Optional.of(override)));
  }

  @Test
  void aContainerThatIsNotDeniedIsEligibleWithoutBeingListedAnywhere() {
    assertTrue(NetworkEligibilityRules.isEligible(true, false, Optional.empty()));
  }

  @Test
  void anOperatorCanAdmitABlockTheDefaultsDeny() {
    assertTrue(NetworkEligibilityRules.isEligible(true, true, Optional.of(true)));
  }

  @Test
  void anOperatorCanExcludeABlockTheDefaultsAdmit() {
    assertFalse(NetworkEligibilityRules.isEligible(true, false, Optional.of(false)));
  }
}
