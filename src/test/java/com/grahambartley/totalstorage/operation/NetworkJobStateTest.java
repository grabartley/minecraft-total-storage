package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NetworkJobStateTest {
  @Test
  void onlyARunningJobReportsItselfRunning() {
    assertTrue(NetworkJobState.RUNNING.isRunning());
    assertFalse(NetworkJobState.DONE.isRunning());
  }

  @ParameterizedTest
  @EnumSource(NetworkJobState.class)
  void everyStateSurvivesARoundTripThroughItsName(NetworkJobState state) {
    assertEquals(state, NetworkJobState.valueOf(state.name()));
  }
}
