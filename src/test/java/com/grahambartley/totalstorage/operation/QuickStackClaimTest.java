package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuickStackClaimTest {
  private static QuickStackClaim claimOf(ContainerRole role) {
    return new QuickStackClaim(ContainerAssignment.of(role), NetworkTagSet.defaults());
  }

  @ParameterizedTest
  @CsvSource({
    "GENERAL, EXCLUDED, EXCLUDED",
    "EXCLUDED, GENERAL, EXCLUDED",
    "GENERAL, FILTERED, FILTERED",
    "FILTERED, GENERAL, FILTERED",
    "FILTERED, EXCLUDED, EXCLUDED",
    "GENERAL, GENERAL, GENERAL"
  })
  void theStrictestOfTwoTerminalsDecidesWhatMayBeStacked(
      String mine, String theirs, String expected) {
    QuickStackClaim strictest =
        claimOf(ContainerRole.valueOf(mine)).strictest(claimOf(ContainerRole.valueOf(theirs)));

    assertEquals(ContainerRole.valueOf(expected), strictest.assignment().role());
  }

  @Test
  void aClaimWithNothingToCompareAgainstStandsAsItIs() {
    QuickStackClaim claim = claimOf(ContainerRole.GENERAL);

    assertSame(claim, claim.strictest(null));
  }

  @Test
  void aClaimFallsBackToTheNetworksDefaultTagsWhenGivenNone() {
    QuickStackClaim claim = new QuickStackClaim(ContainerAssignment.DEFAULT, null);

    assertEquals(NetworkTagSet.defaults(), claim.tags());
  }

  @Test
  void aClaimNeedsAnAssignment() {
    assertThrows(
        NullPointerException.class, () -> new QuickStackClaim(null, NetworkTagSet.defaults()));
  }

  @Test
  void aFilteredClaimKeepsTheFilterItWasGiven() {
    QuickStackClaim claim =
        new QuickStackClaim(
            ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:cobblestone")),
            NetworkTagSet.defaults());

    assertEquals(ContainerRole.FILTERED, claim.assignment().role());
    assertEquals(ContainerFilter.ofItems("minecraft:cobblestone"), claim.assignment().filter());
  }
}
