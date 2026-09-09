package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.storage.ContainerRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerRoleCycleTest {
  @ParameterizedTest
  @CsvSource({"GENERAL, FILTERED", "FILTERED, EXCLUDED", "EXCLUDED, GENERAL"})
  void clickingTightensWhatTheNetworkMayDo(ContainerRole from, ContainerRole to) {
    assertEquals(to, ContainerRoleCycle.next(from));
  }

  @ParameterizedTest
  @CsvSource({"FILTERED, GENERAL", "EXCLUDED, FILTERED", "GENERAL, EXCLUDED"})
  void rightClickingWalksBackTheWayItCame(ContainerRole from, ContainerRole to) {
    assertEquals(to, ContainerRoleCycle.previous(from));
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void goingForwardsThenBackReturnsToWhereItStarted(ContainerRole role) {
    assertEquals(role, ContainerRoleCycle.previous(ContainerRoleCycle.next(role)));
    assertEquals(role, ContainerRoleCycle.next(ContainerRoleCycle.previous(role)));
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void threeClicksReturnToWhereItStarted(ContainerRole role) {
    assertEquals(
        role, ContainerRoleCycle.next(ContainerRoleCycle.next(ContainerRoleCycle.next(role))));
  }

  @Test
  void aContainerWithNoRoleIsCycledFromTheDefaultOne() {
    assertEquals(ContainerRoleCycle.next(ContainerRole.DEFAULT), ContainerRoleCycle.next(null));
    assertEquals(
        ContainerRoleCycle.previous(ContainerRole.DEFAULT), ContainerRoleCycle.previous(null));
  }
}
