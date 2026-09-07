package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DepositTargetTest {
  private static final BlockPos SOMEWHERE = new BlockPos(4, 64, 7);
  private static final String COBBLESTONE = "minecraft:cobblestone";

  @Test
  void aGeneralContainerTakesAnythingItIsOffered() {
    DepositTarget target = general(false);

    assertTrue(target.accepts(COBBLESTONE, Set.of()));
    assertTrue(target.accepts("minecraft:diamond", Set.of("ores_and_minerals")));
  }

  /**
   * ADR 0004: excluded exists so a curated container is never written to, under any configuration.
   */
  @Test
  void anExcludedContainerTakesNothingHoweverItIsFiltered() {
    DepositTarget target =
        new DepositTarget(
            SOMEWHERE, ContainerRole.EXCLUDED, ContainerFilter.ofItems(COBBLESTONE), true);

    assertFalse(target.accepts(COBBLESTONE, Set.of()));
  }

  @Test
  void aFilteredContainerTakesOnlyWhatItNamed() {
    DepositTarget target =
        new DepositTarget(
            SOMEWHERE, ContainerRole.FILTERED, ContainerFilter.ofItems(COBBLESTONE), false);

    assertTrue(target.accepts(COBBLESTONE, Set.of()));
    assertFalse(target.accepts("minecraft:diamond", Set.of()));
  }

  @Test
  void aFilteredContainerTakesWhatOneOfItsTagsCovers() {
    DepositTarget target =
        new DepositTarget(
            SOMEWHERE, ContainerRole.FILTERED, ContainerFilter.ofTags("ores_and_minerals"), false);

    assertTrue(target.accepts("minecraft:diamond", Set.of("ores_and_minerals")));
    assertFalse(target.accepts("minecraft:oak_log", Set.of("wood")));
  }

  /** A filtered container that has named nothing is not a fallback for everything. */
  @Test
  void aFilteredContainerWithAnEmptyFilterTakesNothing() {
    DepositTarget target =
        new DepositTarget(SOMEWHERE, ContainerRole.FILTERED, ContainerFilter.NONE, false);

    assertFalse(target.accepts(COBBLESTONE, Set.of()));
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void aTargetWithNoFilterIsStillAnsweredRatherThanThrowing(ContainerRole role) {
    DepositTarget target = new DepositTarget(SOMEWHERE, role, null, false);

    assertEquals(ContainerFilter.NONE, target.filter());
    assertEquals(role == ContainerRole.GENERAL, target.accepts(COBBLESTONE, null));
  }

  @Test
  void aTargetWithNoRoleIsTreatedAsTheDefaultOne() {
    assertEquals(ContainerRole.DEFAULT, new DepositTarget(SOMEWHERE, null, null, false).role());
  }

  private static DepositTarget general(boolean holdsItem) {
    return new DepositTarget(SOMEWHERE, ContainerRole.GENERAL, ContainerFilter.NONE, holdsItem);
  }
}
