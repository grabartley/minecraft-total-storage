package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * That a cap says its piece once and then stops.
 *
 * <p>Asserted through what {@code reached} answers rather than by capturing the log, because
 * whether this was the first time is the rule itself and the line is only what the rule produces.
 *
 * <p>The rule matters because a network big enough to reach a cap reaches it on every request. A
 * line per occurrence would bury the one line a pack author needed under thousands of copies of
 * itself, which is the failure this class exists to prevent.
 */
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

  /** Two different caps are two different pieces of news, and a server hitting both hears both. */
  @Test
  void adifferentCapIsItsOwnReport() {
    assertTrue(PayloadCapLog.reached("trusted players", 64, 500));
    assertTrue(PayloadCapLog.reached("highlighted containers", 256, 900));
  }

  /** A fresh server reports afresh, so a restart is not silence about a cap still being hit. */
  @Test
  void clearingLetsACapReportAgain() {
    PayloadCapLog.reached("trusted players", 64, 500);

    PayloadCapLog.clear();

    assertTrue(PayloadCapLog.reached("trusted players", 64, 500));
  }
}
