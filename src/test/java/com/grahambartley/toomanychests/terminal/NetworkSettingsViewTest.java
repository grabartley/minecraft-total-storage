package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class NetworkSettingsViewTest {
  private static ContainerRoleEntry row(int index) {
    return new ContainerRoleEntry(
        new BlockPos(index, 64, 0), ContainerRole.GENERAL, Optional.empty(), 1, 1L, 1, 27, index);
  }

  private static NetworkSettingsView view(
      int radius, int effective, int offset, int total, int rows) {
    return new NetworkSettingsView(
        radius,
        effective,
        32,
        TerminalAccessMode.OWNER,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        offset,
        total,
        IntStream.range(0, rows).mapToObj(NetworkSettingsViewTest::row).toList());
  }

  @Test
  void aTerminalHeldUnderItsOwnSettingIsClamped() {
    assertTrue(view(24, 16, 0, 0, 0).isClamped());
    assertFalse(view(16, 16, 0, 0, 0).isClamped());
  }

  @Test
  void aRowIsFoundByItsPlaceInTheWholeListRatherThanInThePage() {
    NetworkSettingsView view = view(16, 16, 10, 40, 4);

    assertEquals(new BlockPos(0, 64, 0), view.at(10).container());
    assertEquals(new BlockPos(3, 64, 0), view.at(13).container());
    assertNull(view.at(9));
    assertNull(view.at(14));
  }

  @Test
  void aPanelWithNoContainersSaysSoRatherThanLookingUnanswered() {
    assertFalse(view(16, 16, 0, 0, 0).hasContainers());
    assertTrue(view(16, 16, 0, 3, 3).hasContainers());
  }

  @Test
  void aPendingPanelIsNotAdministrableAndHoldsNothing() {
    NetworkSettingsView pending = NetworkSettingsView.pending();

    assertFalse(pending.mayAdminister());
    assertFalse(pending.powered());
    assertFalse(pending.hasContainers());
    assertTrue(pending.trusted().isEmpty());
  }

  @Test
  void aViewLargerThanAPacketShouldCarryIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> view(16, 16, 0, 1000, NetworkSettingsView.MAX_CONTAINERS + 1));
  }

  @Test
  void aTrustListLargerThanAPacketShouldCarryIsRefused() {
    List<String> tooMany =
        IntStream.rangeClosed(0, NetworkSettingsView.MAX_TRUSTED)
            .mapToObj(Integer::toString)
            .toList();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new NetworkSettingsView(
                16,
                16,
                32,
                TerminalAccessMode.OWNER,
                true,
                true,
                tooMany,
                NetworkCapacity.EMPTY,
                0,
                0,
                List.of()));
  }

  @Test
  void anOffsetOrTotalBelowZeroBecomesZero() {
    NetworkSettingsView view = view(16, 16, -5, -5, 0);

    assertEquals(0, view.containerOffset());
    assertEquals(0, view.containerTotal());
  }

  @Test
  void aViewMissingItsPartsFallsBackRatherThanCarryingNulls() {
    NetworkSettingsView view =
        new NetworkSettingsView(16, 16, 32, null, false, false, null, null, 0, 0, null);

    assertEquals(TerminalAccessMode.OWNER, view.accessMode());
    assertTrue(view.trusted().isEmpty());
    assertTrue(view.containers().isEmpty());
  }
}
