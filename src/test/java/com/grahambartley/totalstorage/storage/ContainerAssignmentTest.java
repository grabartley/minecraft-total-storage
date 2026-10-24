package com.grahambartley.totalstorage.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerAssignmentTest {
  private static final ContainerFilter ORES = ContainerFilter.ofTags("ores");

  @Test
  void theDefaultAssignmentIsTheOneThatChangesNothing() {
    assertEquals(ContainerRole.DEFAULT, ContainerAssignment.DEFAULT.role());
    assertEquals(ContainerFilter.NONE, ContainerAssignment.DEFAULT.filter());
    assertTrue(ContainerAssignment.DEFAULT.isDefault());
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void anAssignmentKeepsTheRoleItWasGiven(ContainerRole role) {
    assertEquals(role, ContainerAssignment.of(role).role());
  }

  @Test
  void aNullRoleIsReadAsTheDefault() {
    assertEquals(ContainerAssignment.DEFAULT, ContainerAssignment.of(null));
    assertEquals(ContainerRole.DEFAULT, new ContainerAssignment(null, ORES).role());
  }

  @Test
  void aFilteredContainerKeepsWhatItAccepts() {
    ContainerAssignment filtered = ContainerAssignment.filtered(ORES);

    assertEquals(ContainerRole.FILTERED, filtered.role());
    assertEquals(ORES, filtered.filter());
    assertFalse(filtered.isDefault());
  }

  @ParameterizedTest
  @EnumSource(
      value = ContainerRole.class,
      names = {"GENERAL", "EXCLUDED"})
  void aRoleThatCannotUseAFilterNeverCarriesOne(ContainerRole role) {
    assertEquals(ContainerFilter.NONE, new ContainerAssignment(role, ORES).filter());
  }

  @Test
  void demotingAFilteredContainerDropsWhatItAccepted() {
    ContainerAssignment demoted =
        ContainerAssignment.filtered(ORES).withRole(ContainerRole.EXCLUDED);

    assertEquals(ContainerRole.EXCLUDED, demoted.role());
    assertEquals(ContainerFilter.NONE, demoted.filter());
  }

  @Test
  void aFilterRestoredAfterADemotionIsGoneRatherThanRemembered() {
    ContainerAssignment promotedAgain =
        ContainerAssignment.filtered(ORES)
            .withRole(ContainerRole.GENERAL)
            .withRole(ContainerRole.FILTERED);

    assertEquals(ContainerFilter.NONE, promotedAgain.filter());
  }

  @Test
  void aNullFilterIsReadAsAcceptingNothing() {
    assertEquals(
        ContainerFilter.NONE, new ContainerAssignment(ContainerRole.FILTERED, null).filter());
  }

  @Test
  void assigningTheRoleItAlreadyHasChangesNothing() {
    ContainerAssignment filtered = ContainerAssignment.filtered(ORES);

    assertSame(filtered, filtered.withRole(ContainerRole.FILTERED));
  }

  @Test
  void aFilteredContainerThatAcceptsNothingIsStillNotTheDefault() {
    assertFalse(ContainerAssignment.filtered(ContainerFilter.NONE).isDefault());
  }

  @Test
  void aNameIsSanitisedIntoTheAssignmentRatherThanStoredRaw() {
    assertEquals(
        "Brewing stand",
        new ContainerAssignment(ContainerRole.GENERAL, ContainerFilter.NONE, "  Brewing   stand ")
            .name());
  }

  @Test
  void anAssignmentThatOnlyCarriesANameIsNotTheDefault() {
    ContainerAssignment named = ContainerAssignment.DEFAULT.withName("Potions");

    assertTrue(named.isNamed());
    assertFalse(named.isDefault());
  }

  @Test
  void aNameSurvivesEveryRoleTheContainerIsCycledThrough() {
    ContainerAssignment named = ContainerAssignment.DEFAULT.withName("Potions");

    assertEquals(
        "Potions", named.withRole(ContainerRole.FILTERED).withRole(ContainerRole.EXCLUDED).name());
  }

  @Test
  void renamingToTheNameItAlreadyHasChangesNothing() {
    ContainerAssignment named = ContainerAssignment.DEFAULT.withName("Potions");

    assertSame(named, named.withName(" Potions "));
  }

  @Test
  void clearingTheNameLeavesAnOtherwiseUntouchedContainerAtTheDefault() {
    assertTrue(ContainerAssignment.DEFAULT.withName("Potions").withName("").isDefault());
  }

  @Test
  void twoContainersConfiguredTheSameWayCompareEqual() {
    ContainerAssignment first =
        ContainerAssignment.filtered(ContainerFilter.ofTags("ores", "wood"));
    ContainerAssignment second =
        ContainerAssignment.of(ContainerRole.FILTERED)
            .withFilter(ContainerFilter.NONE.withTag("wood").withTag("ores"));

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }
}
