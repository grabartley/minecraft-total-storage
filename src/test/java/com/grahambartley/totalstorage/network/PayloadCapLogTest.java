package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PayloadCapLogTest {
  @BeforeEach
  @AfterEach
  void forgetWhatWasReported() {
    PayloadCapLog.clear();
  }

  @Test
  void reachingACapIsReportedOnceHoweverOftenItIsReached() {
    assertTrue(PayloadCapLog.reached("trusted players", 64, 500));
    assertFalse(PayloadCapLog.reached("trusted players", 64, 500));
    assertFalse(PayloadCapLog.reached("trusted players", 64, 900));
  }

  @Test
  void adifferentCapIsItsOwnReport() {
    assertTrue(PayloadCapLog.reached("trusted players", 64, 500));
    assertTrue(PayloadCapLog.reached("highlighted containers", 256, 900));
  }

  @Test
  void clearingLetsACapReportAgain() {
    PayloadCapLog.reached("trusted players", 64, 500);

    PayloadCapLog.clear();

    assertTrue(PayloadCapLog.reached("trusted players", 64, 500));
  }
}
