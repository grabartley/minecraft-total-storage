package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalOpenDataTest {
  private static final BlockPos SOMEWHERE = new BlockPos(12, 64, -30);

  @ParameterizedTest
  @CsvSource({
    "-40, " + TerminalSettings.MIN_RADIUS,
    "0, " + TerminalSettings.MIN_RADIUS,
    "8, 8",
    "10000, " + TerminalSettings.MAX_RADIUS
  })
  void aRadiusOutsideWhatATerminalCanReachIsClampedRatherThanTrusted(int given, int expected) {
    assertEquals(expected, data(given).radius());
  }

  @Test
  void aPayloadWithNoPermissionGrantsTheLesserOne() {
    TerminalOpenData data = new TerminalOpenData(SOMEWHERE, 8, null, NetworkTagSet.defaults());

    assertEquals(TerminalPermission.USE, data.permission());
    assertFalse(data.mayAdminister());
  }

  @Test
  void aPayloadWithNoTagSetStillCarriesThePresets() {
    TerminalOpenData data = new TerminalOpenData(SOMEWHERE, 8, TerminalPermission.USE, null);

    assertEquals(NetworkTagSet.defaults(), data.tags());
    assertFalse(data.tags().all().isEmpty());
  }

  @Test
  void aPayloadNamingNoTerminalIsRefusedOutright() {
    assertThrows(
        NullPointerException.class,
        () -> new TerminalOpenData(null, 8, TerminalPermission.USE, NetworkTagSet.defaults()));
  }

  @Test
  void theTerminalPositionIsCopiedSoItCannotBeMovedUnderTheScreen() {
    BlockPos.Mutable moving = new BlockPos.Mutable(1, 2, 3);
    TerminalOpenData data =
        new TerminalOpenData(moving, 8, TerminalPermission.USE, NetworkTagSet.defaults());

    moving.set(90, 90, 90);

    assertEquals(new BlockPos(1, 2, 3), data.terminal());
  }

  @Test
  void aPlayerGivenTheAdministerPermissionMayAdminister() {
    assertTrue(
        new TerminalOpenData(SOMEWHERE, 8, TerminalPermission.ADMINISTER, NetworkTagSet.defaults())
            .mayAdminister());
  }

  private static TerminalOpenData data(int radius) {
    return new TerminalOpenData(
        SOMEWHERE, radius, TerminalPermission.USE, NetworkTagSet.defaults());
  }
}
