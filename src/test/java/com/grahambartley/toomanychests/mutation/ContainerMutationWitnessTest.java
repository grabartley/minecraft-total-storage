package com.grahambartley.toomanychests.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class ContainerMutationWitnessTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));

  private static final StorageOrigin CHEST = originAt(0);
  private static final StorageOrigin BARREL = originAt(1);
  private static final StorageOrigin ELSEWHERE = originAt(2);

  private final ContainerMutationRegistry registry = new ContainerMutationRegistry();

  @Test
  void aWitnessThatSawNothingReportsNothing() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST, BARREL))) {
      assertFalse(witness.sawAnything());
      assertEquals(List.of(), List.copyOf(witness.changed()));
    }
  }

  @Test
  void aMutationOfAWatchedContainerIsWitnessed() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST, BARREL))) {
      registry.publish(OVERWORLD, new BlockPos(0, 64, 0));

      assertTrue(witness.sawAnything());
      assertEquals(List.of(CHEST), List.copyOf(witness.changed()));
    }
  }

  @Test
  void aMutationOfAContainerNobodyNamedIsNotWitnessed() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST))) {
      registry.publish(OVERWORLD, new BlockPos(2, 64, 0));

      assertFalse(witness.sawAnything());
    }
  }

  @Test
  void containersAreReportedInTheOrderTheyAnnouncedThemselves() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST, BARREL, ELSEWHERE))) {
      registry.publish(OVERWORLD, new BlockPos(2, 64, 0));
      registry.publish(OVERWORLD, new BlockPos(0, 64, 0));

      assertEquals(List.of(ELSEWHERE, CHEST), List.copyOf(witness.changed()));
    }
  }

  @Test
  void aContainerThatChangedTwiceIsReportedOnce() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST))) {
      registry.publish(OVERWORLD, new BlockPos(0, 64, 0));
      registry.publish(OVERWORLD, new BlockPos(0, 64, 0));

      assertEquals(List.of(CHEST), List.copyOf(witness.changed()));
    }
  }

  @Test
  void closingLeavesTheRegistryAsItWasFound() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST, BARREL))) {
      assertEquals(2, registry.watchedContainerCount());
    }

    assertTrue(registry.isIdle());
  }

  @Test
  void aClosedWitnessHearsNothingMore() {
    ContainerMutationWitness witness = ContainerMutationWitness.over(registry, List.of(CHEST));
    witness.close();

    registry.publish(OVERWORLD, new BlockPos(0, 64, 0));

    assertFalse(witness.sawAnything());
  }

  @Test
  void closingTwiceIsHarmless() {
    ContainerMutationWitness witness = ContainerMutationWitness.over(registry, List.of(CHEST));
    witness.close();
    witness.close();

    assertTrue(registry.isIdle());
  }

  @Test
  void theSameContainerNamedTwiceIsWatchedOnce() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST, CHEST))) {
      assertEquals(List.of(CHEST), witness.watchedContainers());
    }
  }

  @Test
  void witnessingNothingWatchesNothing() {
    try (ContainerMutationWitness witness = ContainerMutationWitness.over(registry, List.of())) {
      assertTrue(registry.isIdle());
      assertFalse(witness.sawAnything());
    }
  }

  @Test
  void aNullSetOfContainersIsAnEmptyOne() {
    try (ContainerMutationWitness witness = ContainerMutationWitness.over(registry, null)) {
      assertTrue(registry.isIdle());
      assertEquals(List.of(), witness.watchedContainers());
    }
  }

  @Test
  void aWitnessNeedsARegistryToListenTo() {
    assertThrows(
        NullPointerException.class, () -> ContainerMutationWitness.over(null, List.of(CHEST)));
  }

  @Test
  void whatAWitnessReportsCannotBeEditedByItsCaller() {
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(registry, List.of(CHEST))) {
      registry.publish(OVERWORLD, new BlockPos(0, 64, 0));

      assertThrows(UnsupportedOperationException.class, () -> witness.changed().clear());
    }
  }

  private static StorageOrigin originAt(int x) {
    return new BlockStorageOrigin(OVERWORLD, new BlockPos(x, 64, 0));
  }
}
