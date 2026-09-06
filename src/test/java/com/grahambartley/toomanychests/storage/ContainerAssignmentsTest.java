package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerAssignmentsTest {
  private static final BlockPos BARREL = new BlockPos(3, 64, -7);
  private static final BlockPos CHEST = new BlockPos(-11, 12, 40);
  private static final ContainerFilter ORES = ContainerFilter.ofTags("ores");

  @Test
  void aNetworkNobodyConfiguredHasAnOpinionAboutNothing() {
    assertTrue(ContainerAssignments.empty().isEmpty());
    assertEquals(0, ContainerAssignments.empty().size());
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void aRoleIsRememberedPerContainer(ContainerRole role) {
    ContainerAssignments assignments = ContainerAssignments.empty().withRole(BARREL, role);

    assertEquals(role, assignments.roleFor(BARREL));
    assertEquals(ContainerRole.DEFAULT, assignments.roleFor(CHEST));
  }

  @Test
  void anUnconfiguredContainerReadsAsTheDefaultAssignment() {
    assertEquals(ContainerAssignment.DEFAULT, ContainerAssignments.empty().forContainer(BARREL));
    assertEquals(ContainerAssignment.DEFAULT, ContainerAssignments.empty().forContainer(null));
    assertEquals(ContainerFilter.NONE, ContainerAssignments.empty().filterFor(BARREL));
  }

  @Test
  void aFilterIsRememberedAlongsideTheRoleThatUsesIt() {
    ContainerAssignments assignments =
        ContainerAssignments.empty()
            .withRole(CHEST, ContainerRole.FILTERED)
            .withFilter(CHEST, ORES);

    assertEquals(ContainerRole.FILTERED, assignments.roleFor(CHEST));
    assertEquals(ORES, assignments.filterFor(CHEST));
  }

  @Test
  void aFilterGivenToAContainerThatCannotUseOneIsNotStored() {
    ContainerAssignments assignments = ContainerAssignments.empty().withFilter(BARREL, ORES);

    assertTrue(assignments.isEmpty());
    assertEquals(ContainerFilter.NONE, assignments.filterFor(BARREL));
  }

  @Test
  void givingAContainerTheDefaultAssignmentForgetsItRatherThanRecordingIt() {
    ContainerAssignments assignments =
        ContainerAssignments.empty()
            .withRole(BARREL, ContainerRole.EXCLUDED)
            .withRole(BARREL, ContainerRole.GENERAL);

    assertTrue(assignments.isEmpty());
    assertEquals(ContainerAssignments.empty(), assignments);
  }

  @Test
  void assigningWhatAContainerAlreadyHasChangesNothing() {
    ContainerAssignments assignments =
        ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED);

    assertSame(assignments, assignments.withRole(BARREL, ContainerRole.EXCLUDED));
    assertSame(assignments, assignments.withRole(null, ContainerRole.EXCLUDED));
    assertSame(
        assignments, assignments.with(BARREL, ContainerAssignment.of(ContainerRole.EXCLUDED)));
  }

  @Test
  void aNullAssignmentIsReadAsTheDefaultRatherThanBeingStored() {
    ContainerAssignments assignments =
        ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED).with(BARREL, null);

    assertTrue(assignments.isEmpty());
  }

  @Test
  void forgettingAContainerLeavesItAtTheDefault() {
    ContainerAssignments assignments =
        ContainerAssignments.empty()
            .with(BARREL, ContainerAssignment.filtered(ORES))
            .withRole(CHEST, ContainerRole.EXCLUDED);

    ContainerAssignments forgotten = assignments.without(BARREL);

    assertEquals(ContainerAssignment.DEFAULT, forgotten.forContainer(BARREL));
    assertEquals(ContainerRole.EXCLUDED, forgotten.roleFor(CHEST));
    assertEquals(1, forgotten.size());
  }

  @Test
  void forgettingAContainerNobodyConfiguredChangesNothing() {
    ContainerAssignments assignments =
        ContainerAssignments.empty().withRole(CHEST, ContainerRole.EXCLUDED);

    assertSame(assignments, assignments.without(BARREL));
    assertSame(assignments, assignments.without(null));
  }

  @Test
  void assignmentsAreOrderedByPositionWhateverOrderTheyArrivedIn() {
    List<BlockPos> forwards =
        List.copyOf(
            ContainerAssignments.empty()
                .withRole(BARREL, ContainerRole.EXCLUDED)
                .withRole(CHEST, ContainerRole.FILTERED)
                .byPosition()
                .keySet());
    List<BlockPos> backwards =
        List.copyOf(
            ContainerAssignments.empty()
                .withRole(CHEST, ContainerRole.FILTERED)
                .withRole(BARREL, ContainerRole.EXCLUDED)
                .byPosition()
                .keySet());

    assertEquals(forwards, backwards);
  }

  @Test
  void nullsAndDefaultsInsideAMapAreDroppedRatherThanStored() {
    Map<BlockPos, ContainerAssignment> raw = new HashMap<>();
    raw.put(BARREL, null);
    raw.put(null, ContainerAssignment.of(ContainerRole.EXCLUDED));
    raw.put(new BlockPos(1, 2, 3), ContainerAssignment.DEFAULT);
    raw.put(CHEST, ContainerAssignment.filtered(ORES));

    ContainerAssignments assignments = new ContainerAssignments(raw);

    assertEquals(Map.of(CHEST, ContainerAssignment.filtered(ORES)), assignments.byPosition());
  }

  @Test
  void aNullMapIsReadAsAnEmptyNetwork() {
    assertEquals(ContainerAssignments.empty(), new ContainerAssignments(null));
  }

  @Test
  void theStoredMapRefusesToBeChangedFromOutside() {
    ContainerAssignments assignments =
        ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED);

    assertThrows(
        UnsupportedOperationException.class,
        () -> assignments.byPosition().put(CHEST, ContainerAssignment.DEFAULT));
  }

  @Test
  void twoNetworksConfiguredTheSameWayCompareEqual() {
    ContainerAssignments first =
        ContainerAssignments.empty()
            .withRole(BARREL, ContainerRole.EXCLUDED)
            .withRole(CHEST, ContainerRole.FILTERED)
            .withFilter(CHEST, ORES);
    ContainerAssignments second =
        ContainerAssignments.empty()
            .with(CHEST, ContainerAssignment.filtered(ORES))
            .withRole(BARREL, ContainerRole.EXCLUDED);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void aMutablePositionCannotCorruptTheMapAfterItIsStored() {
    BlockPos.Mutable moving = new BlockPos.Mutable(3, 64, -7);
    ContainerAssignments assignments =
        ContainerAssignments.empty().withRole(moving, ContainerRole.EXCLUDED);

    moving.set(0, 0, 0);

    assertEquals(ContainerRole.EXCLUDED, assignments.roleFor(BARREL));
    assertEquals(ContainerRole.DEFAULT, assignments.roleFor(new BlockPos(0, 0, 0)));
  }
}
