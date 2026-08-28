package com.grahambartley.toomanychests.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class DeferredInvalidationsTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final StorageOrigin FIRST =
      new BlockStorageOrigin(OVERWORLD, new BlockPos(0, 64, 0));
  private static final StorageOrigin SECOND =
      new BlockStorageOrigin(OVERWORLD, new BlockPos(1, 64, 0));

  private final List<StorageOrigin> invalidated = new ArrayList<>();
  private final DeferredInvalidations invalidations = new DeferredInvalidations(invalidated::add);

  @Test
  void nothingReachesTheIndexUntilTheTransactionCommits() {
    try (Transaction outer = Transaction.openOuter()) {
      invalidations.record(FIRST, outer);
      assertTrue(invalidated.isEmpty());
      outer.commit();
    }

    assertEquals(List.of(FIRST), invalidated);
  }

  @Test
  void anAbortedTransactionInvalidatesNothing() {
    try (Transaction outer = Transaction.openOuter()) {
      invalidations.record(FIRST, outer);
      invalidations.record(SECOND, outer);
      outer.abort();
    }

    assertEquals(List.of(), invalidated);
    assertEquals(0, invalidations.pendingCount());
  }

  @Test
  void aContainerTouchedRepeatedlyIsInvalidatedOnce() {
    try (Transaction outer = Transaction.openOuter()) {
      invalidations.record(FIRST, outer);
      invalidations.record(FIRST, outer);
      invalidations.record(SECOND, outer);
      invalidations.record(FIRST, outer);
      outer.commit();
    }

    assertEquals(List.of(FIRST, SECOND), invalidated);
  }

  @Test
  void anAbandonedStepDropsItsOwnRecordsAndKeepsTheRest() {
    try (Transaction outer = Transaction.openOuter()) {
      invalidations.record(FIRST, outer);
      try (Transaction step = outer.openNested()) {
        invalidations.record(SECOND, step);
        step.abort();
      }
      assertEquals(1, invalidations.pendingCount());
      outer.commit();
    }

    assertEquals(List.of(FIRST), invalidated);
  }

  @Test
  void aCommittedStepIsInvalidatedWithTheTransactionThatContainsIt() {
    try (Transaction outer = Transaction.openOuter()) {
      try (Transaction step = outer.openNested()) {
        invalidations.record(SECOND, step);
        step.commit();
      }
      assertTrue(invalidated.isEmpty());
      outer.commit();
    }

    assertEquals(List.of(SECOND), invalidated);
  }
}
