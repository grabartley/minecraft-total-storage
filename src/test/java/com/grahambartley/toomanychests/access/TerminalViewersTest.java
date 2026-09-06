package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Set;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalViewersTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  private static final TerminalAddress BASE =
      new TerminalAddress(OVERWORLD, new BlockPos(4, 64, -12));
  private static final TerminalAddress OUTPOST =
      new TerminalAddress(OVERWORLD, new BlockPos(900, 70, 900));
  private static final TerminalAddress ACROSS_A_PORTAL =
      new TerminalAddress(NETHER, new BlockPos(4, 64, -12));

  private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

  private TerminalViewers viewers;

  @BeforeEach
  void setUp() {
    viewers = new TerminalViewers();
  }

  @Test
  void aTerminalNobodyIsLookingAtHasNoViewers() {
    assertEquals(Set.of(), viewers.viewing(BASE));
    assertFalse(viewers.isViewing(BASE, ALICE));
  }

  @Test
  void aPlayerWhoOpensATerminalIsRecordedAsViewingIt() {
    viewers.opened(BASE, ALICE);

    assertTrue(viewers.isViewing(BASE, ALICE));
    assertEquals(Set.of(ALICE), viewers.viewing(BASE));
  }

  @Test
  void twoPlayersAtTheSameTerminalAreBothRecorded() {
    viewers.opened(BASE, ALICE);
    viewers.opened(BASE, BOB);

    assertEquals(Set.of(ALICE, BOB), viewers.viewing(BASE));
  }

  @Test
  void openingTheSameTerminalTwiceRecordsThePlayerOnce() {
    viewers.opened(BASE, ALICE);
    viewers.opened(BASE, ALICE);

    assertEquals(Set.of(ALICE), viewers.viewing(BASE));
  }

  @Test
  void closingOneScreenLeavesTheOtherViewerAlone() {
    viewers.opened(BASE, ALICE);
    viewers.opened(BASE, BOB);

    viewers.closed(BASE, ALICE);

    assertEquals(Set.of(BOB), viewers.viewing(BASE));
  }

  @Test
  void closingAScreenNobodyHadOpenChangesNothing() {
    viewers.opened(BASE, ALICE);

    viewers.closed(BASE, BOB);
    viewers.closed(OUTPOST, ALICE);

    assertEquals(Set.of(ALICE), viewers.viewing(BASE));
  }

  /** Two terminals at the same coordinates in different worlds are different terminals. */
  @Test
  void viewersAreKeptPerTerminalRatherThanPerPosition() {
    viewers.opened(BASE, ALICE);
    viewers.opened(ACROSS_A_PORTAL, BOB);

    assertEquals(Set.of(ALICE), viewers.viewing(BASE));
    assertEquals(Set.of(BOB), viewers.viewing(ACROSS_A_PORTAL));
  }

  @Test
  void aPlayerLoggingOutLeavesEveryTerminalTheyHadOpen() {
    viewers.opened(BASE, ALICE);
    viewers.opened(OUTPOST, ALICE);
    viewers.opened(BASE, BOB);

    viewers.closedEverything(ALICE);

    assertEquals(Set.of(BOB), viewers.viewing(BASE));
    assertEquals(Set.of(), viewers.viewing(OUTPOST));
  }

  @Test
  void clearingForgetsEveryTerminal() {
    viewers.opened(BASE, ALICE);
    viewers.opened(OUTPOST, BOB);

    viewers.clear();

    assertEquals(Set.of(), viewers.viewing(BASE));
    assertEquals(Set.of(), viewers.viewing(OUTPOST));
  }

  @Test
  void nothingIsRecordedWithoutATerminalOrAPlayer() {
    viewers.opened(null, ALICE);
    viewers.opened(BASE, null);
    viewers.closed(null, ALICE);
    viewers.closed(BASE, null);
    viewers.closedEverything(null);

    assertEquals(Set.of(), viewers.viewing(BASE));
    assertEquals(Set.of(), viewers.viewing(null));
  }

  /** Handing out the live set would let a caller quietly evict a viewer the sweep still owns. */
  @Test
  void theViewerListHandedOutCannotBeEdited() {
    viewers.opened(BASE, ALICE);

    assertThrows(UnsupportedOperationException.class, () -> viewers.viewing(BASE).add(BOB));
  }
}
