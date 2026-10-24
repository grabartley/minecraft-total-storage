package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class OperationConfirmationsTest {
  private static final UUID PLAYER = UUID.nameUUIDFromBytes("player".getBytes());
  private static final UUID SOMEBODY_ELSE = UUID.nameUUIDFromBytes("somebody else".getBytes());

  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("overworld"));

  private static final TerminalAddress TERMINAL =
      new TerminalAddress(OVERWORLD, new BlockPos(1, 2, 3));
  private static final TerminalAddress ANOTHER_TERMINAL =
      new TerminalAddress(OVERWORLD, new BlockPos(9, 2, 3));

  @Test
  void theFirstClickArmsAConfirmationRatherThanClaimingOne() {
    OperationConfirmations confirmations = new OperationConfirmations();

    assertFalse(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L));
    assertFalse(confirmations.isEmpty());
  }

  @Test
  void aSecondClickInsideTheWindowClaimsTheConfirmation() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    assertTrue(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 20L));
  }

  @Test
  void aClaimedConfirmationIsSpentRatherThanLeftArmed() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 20L);

    assertFalse(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 21L));
  }

  @Test
  void aClickAfterTheWindowArmsAFreshConfirmation() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    assertFalse(
        confirmations.claim(
            PLAYER,
            TERMINAL,
            ExpensiveOperation.SORT_NETWORK,
            OperationConfirmations.WINDOW_TICKS + 1L));
  }

  @Test
  void confirmingOneOperationDoesNotConfirmAnother() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    assertFalse(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.CONSOLIDATE, 1L));
  }

  @Test
  void confirmingAtOneTerminalDoesNotConfirmAtAnother() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    assertFalse(confirmations.claim(PLAYER, ANOTHER_TERMINAL, ExpensiveOperation.SORT_NETWORK, 1L));
  }

  @Test
  void onePlayerCannotConfirmWhatAnotherArmed() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    assertFalse(confirmations.claim(SOMEBODY_ELSE, TERMINAL, ExpensiveOperation.SORT_NETWORK, 1L));
  }

  @Test
  void aPlayerWhoLeavesIsForgotten() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);
    confirmations.claim(SOMEBODY_ELSE, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    confirmations.forget(PLAYER);

    assertFalse(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 1L));
    assertTrue(confirmations.claim(SOMEBODY_ELSE, TERMINAL, ExpensiveOperation.SORT_NETWORK, 1L));
  }

  @Test
  void clearingForgetsEverybody() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 0L);

    confirmations.clear();

    assertTrue(confirmations.isEmpty());
  }

  @Test
  void aClockThatRanBackwardsArmsAgainRatherThanConfirming() {
    OperationConfirmations confirmations = new OperationConfirmations();
    confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 100L);

    assertFalse(confirmations.claim(PLAYER, TERMINAL, ExpensiveOperation.SORT_NETWORK, 20L));
  }
}
