package com.grahambartley.totalstorage.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerAcceptanceTest {
  private static final String COBBLESTONE = "minecraft:cobblestone";

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void anExcludedContainerAcceptsNothingAndAGeneralOneAcceptsAnything(ContainerRole role) {
    boolean accepted =
        ContainerAcceptance.accepts(role, ContainerFilter.NONE, COBBLESTONE, Set.of());

    assertTrue(accepted == (role != ContainerRole.EXCLUDED && role != ContainerRole.FILTERED));
  }

  @Test
  void aFilteredContainerAcceptsWhatItsFilterNames() {
    assertTrue(
        ContainerAcceptance.accepts(
            ContainerRole.FILTERED, ContainerFilter.ofItems(COBBLESTONE), COBBLESTONE, Set.of()));
  }

  @Test
  void aFilteredContainerRefusesWhatItsFilterDoesNotName() {
    assertFalse(
        ContainerAcceptance.accepts(
            ContainerRole.FILTERED,
            ContainerFilter.ofItems("minecraft:diamond"),
            COBBLESTONE,
            Set.of()));
  }

  @Test
  void aFilteredContainerAcceptsAnItemFiledUnderATagItNames() {
    assertTrue(
        ContainerAcceptance.accepts(
            ContainerRole.FILTERED, ContainerFilter.ofTags("stone"), COBBLESTONE, Set.of("stone")));
  }

  @Test
  void aMissingRoleIsTreatedAsTheDefaultOne() {
    assertTrue(ContainerAcceptance.accepts(null, null, COBBLESTONE, null));
  }
}
