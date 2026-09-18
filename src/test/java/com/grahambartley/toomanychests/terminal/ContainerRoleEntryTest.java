package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContainerRoleEntryTest {
  private static final BlockPos SOMEWHERE = new BlockPos(8, 64, -3);

  private static ContainerRoleEntry entry(int variants, long items) {
    return new ContainerRoleEntry(
        SOMEWHERE,
        "",
        ContainerRole.GENERAL,
        List.of(),
        Optional.empty(),
        variants,
        items,
        0,
        27,
        4.0);
  }

  @Test
  void aContainerHoldingNothingIsToldApartFromOneHoldingSomething() {
    assertTrue(entry(0, 0L).isEmpty());
    assertFalse(entry(1, 1L).isEmpty());
  }

  @Test
  void aContainerHoldingNothingCarriesNoIcon() {
    assertTrue(entry(0, 0L).icon().isEmpty());
  }

  @Test
  void anIconThatIsMissingIsTreatedAsNoneRatherThanThrowing() {
    assertTrue(
        new ContainerRoleEntry(
                SOMEWHERE, "", ContainerRole.GENERAL, List.of(), null, 0, 0L, 0, 0, 0.0)
            .icon()
            .isEmpty());
  }

  @Test
  void aRoleThatIsMissingBecomesTheDefaultOne() {
    assertEquals(
        ContainerRole.DEFAULT,
        new ContainerRoleEntry(SOMEWHERE, "", null, List.of(), Optional.empty(), 0, 0L, 0, 0, 0.0)
            .role());
  }

  @ParameterizedTest
  @CsvSource({"-1, 0", "0, -1"})
  void countsBelowZeroAreRefusedRatherThanDrawn(int variants, long items) {
    assertThrows(IllegalArgumentException.class, () -> entry(variants, items));
  }

  @Test
  void usingMoreSlotsThanTheContainerHasIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ContainerRoleEntry(
                SOMEWHERE,
                "",
                ContainerRole.GENERAL,
                List.of(),
                Optional.empty(),
                0,
                0L,
                28,
                27,
                0.0));
  }

  @ParameterizedTest
  @CsvSource({"-1, 0", "0, -1"})
  void aSlotCountBelowZeroIsRefused(int used, int total) {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ContainerRoleEntry(
                SOMEWHERE,
                "",
                ContainerRole.GENERAL,
                List.of(),
                Optional.empty(),
                0,
                0L,
                used,
                total,
                0.0));
  }

  @Test
  void aDistanceBelowZeroIsRefusedSinceNothingCanBeNearerThanTouching() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ContainerRoleEntry(
                SOMEWHERE,
                "",
                ContainerRole.GENERAL,
                List.of(),
                Optional.empty(),
                0,
                0L,
                0,
                0,
                -1.0));
  }

  @Test
  void aMutablePositionIsCopiedRatherThanHeld() {
    BlockPos.Mutable moving = new BlockPos.Mutable(1, 2, 3);
    ContainerRoleEntry row =
        new ContainerRoleEntry(
            moving, "", ContainerRole.GENERAL, List.of(), Optional.empty(), 0, 0L, 0, 0, 0.0);

    moving.set(9, 9, 9);

    assertEquals(new BlockPos(1, 2, 3), row.container());
  }
}
