package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoleTallyTest {

  @Test
  void arunOfRolesIsCountedIntoItsThreeKinds() {
    RoleTally tally =
        RoleTally.of(
            List.of(
                ContainerRole.GENERAL,
                ContainerRole.GENERAL,
                ContainerRole.FILTERED,
                ContainerRole.EXCLUDED));

    assertEquals(2, tally.general());
    assertEquals(1, tally.filtered());
    assertEquals(1, tally.excluded());
    assertEquals(4, tally.total());
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleIsCountedUnderItself(ContainerRole role) {
    RoleTally tally = RoleTally.of(List.of(role));

    assertEquals(1, tally.total());
    assertEquals(role == ContainerRole.GENERAL ? 1 : 0, tally.general());
    assertEquals(role == ContainerRole.FILTERED ? 1 : 0, tally.filtered());
    assertEquals(role == ContainerRole.EXCLUDED ? 1 : 0, tally.excluded());
  }

  /** A container the network has never been told about holds the default role rather than none. */
  @Test
  void aRoleThatIsMissingCountsAsTheDefaultOne() {
    RoleTally tally = RoleTally.of(Arrays.asList(null, null));

    assertEquals(2, tally.general());
    assertEquals(2, tally.total());
  }

  @Test
  void aNetworkWithNoContainersTalliesToNothing() {
    assertTrue(RoleTally.of(List.of()).isEmpty());
    assertTrue(RoleTally.of(null).isEmpty());
    assertTrue(RoleTally.EMPTY.isEmpty());
    assertEquals(0, RoleTally.EMPTY.total());
  }

  @Test
  void aTallyCannotCountBelowZero() {
    assertThrows(IllegalArgumentException.class, () -> new RoleTally(-1, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> new RoleTally(0, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> new RoleTally(0, 0, -1));
  }

  @Test
  void aTallyTotalsTheThreeKindsItHolds() {
    assertEquals(15, new RoleTally(3, 5, 7).total());
  }
}
