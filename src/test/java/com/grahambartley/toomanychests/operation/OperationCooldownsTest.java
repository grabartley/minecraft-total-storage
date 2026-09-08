package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Whether an expensive operation may run yet.
 *
 * <p>Every boundary is checked here rather than against a running server, because the failure this
 * guards against is a cooldown one tick out: a limit that eats every other legitimate click reads
 * to a player as the mod having broken, and it is invisible in anything but an exact test.
 */
class OperationCooldownsTest {
  private static final UUID PLAYER = UUID.nameUUIDFromBytes("player".getBytes());
  private static final UUID SOMEBODY_ELSE = UUID.nameUUIDFromBytes("other".getBytes());
  private static final int COOLDOWN = 10;

  @Test
  void theFirstAskAlwaysSucceeds() {
    assertTrue(new OperationCooldowns().claim(PLAYER, ExpensiveOperation.RESCAN, 0L, COOLDOWN));
  }

  @Test
  void askingAgainWithinTheCooldownIsRefused() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);

    assertFalse(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 109L, COOLDOWN));
  }

  /** The tick the cooldown expires on is allowed, or a ten tick limit really lasts eleven. */
  @Test
  void theTickTheCooldownExpiresOnIsAllowed() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);

    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 110L, COOLDOWN));
  }

  /** A refused ask must not restart the wait, or holding the key down would never let go. */
  @Test
  void beingRefusedDoesNotPushTheCooldownFurtherOut() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);
    for (long tick = 101L; tick < 110L; tick++) {
      cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, tick, COOLDOWN);
    }

    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 110L, COOLDOWN));
  }

  /** One player's pace is not another's, or a busy server would rate limit everyone at once. */
  @Test
  void onePlayersCooldownDoesNotBindAnother() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);

    assertTrue(cooldowns.claim(SOMEBODY_ELSE, ExpensiveOperation.RESCAN, 100L, COOLDOWN));
  }

  /** Emptying an inventory must not spend a rescan, or one button appears to break the other. */
  @Test
  void oneOperationsCooldownDoesNotBindAnother() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.DEPOSIT_ALL, 100L, COOLDOWN);

    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN));
  }

  /** The floor of the setting means off rather than one per tick. */
  @ParameterizedTest
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  void aCooldownOfNothingNeverRefuses(int cooldown) {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 5L, cooldown);

    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 5L, cooldown));
    assertEquals(0L, cooldowns.remaining(PLAYER, ExpensiveOperation.RESCAN, 5L, cooldown));
  }

  @Test
  void whatIsLeftToWaitCountsDown() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);

    assertEquals(10L, cooldowns.remaining(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN));
    assertEquals(1L, cooldowns.remaining(PLAYER, ExpensiveOperation.RESCAN, 109L, COOLDOWN));
    assertEquals(0L, cooldowns.remaining(PLAYER, ExpensiveOperation.RESCAN, 110L, COOLDOWN));
  }

  @Test
  void aPlayerWhoHasNeverAskedHasNothingToWaitFor() {
    assertEquals(
        0L, new OperationCooldowns().remaining(PLAYER, ExpensiveOperation.RESCAN, 50L, COOLDOWN));
  }

  /**
   * A world whose clock ran backwards, which a rollback or a re-hosted save can produce. Waiting
   * out a cooldown that expires in the past would take until the clock caught up.
   */
  @Test
  void aClockThatWentBackwardsDoesNotStrandAPlayerOnCooldown() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 1000L, COOLDOWN);

    assertEquals(0L, cooldowns.remaining(PLAYER, ExpensiveOperation.RESCAN, 5L, COOLDOWN));
    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 5L, COOLDOWN));
  }

  @Test
  void forgettingAPlayerLetsThemActAgainAtOnce() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 100L, COOLDOWN);

    cooldowns.forget(PLAYER);

    assertTrue(cooldowns.isEmpty());
    assertTrue(cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 101L, COOLDOWN));
  }

  @Test
  void clearingDropsEveryPlayer() {
    OperationCooldowns cooldowns = new OperationCooldowns();
    cooldowns.claim(PLAYER, ExpensiveOperation.RESCAN, 1L, COOLDOWN);
    cooldowns.claim(SOMEBODY_ELSE, ExpensiveOperation.DEPOSIT_ALL, 1L, COOLDOWN);

    cooldowns.clear();

    assertTrue(cooldowns.isEmpty());
  }

  @ParameterizedTest
  @EnumSource(ExpensiveOperation.class)
  void everyOperationIsRateLimitedIndependently(ExpensiveOperation operation) {
    OperationCooldowns cooldowns = new OperationCooldowns();

    assertTrue(cooldowns.claim(PLAYER, operation, 0L, COOLDOWN));
    assertFalse(cooldowns.claim(PLAYER, operation, 1L, COOLDOWN));
  }

  @Test
  void aCooldownWithoutAPlayerOrAnOperationIsARefusalToGuess() {
    OperationCooldowns cooldowns = new OperationCooldowns();

    assertThrows(
        NullPointerException.class,
        () -> cooldowns.claim(null, ExpensiveOperation.RESCAN, 0L, COOLDOWN));
    assertThrows(NullPointerException.class, () -> cooldowns.claim(PLAYER, null, 0L, COOLDOWN));
  }
}
