package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerScanResultTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));

  private static StorageProvider providerAt(int x) {
    return StorageProvider.of(
        new BlockStorageOrigin(OVERWORLD, new BlockPos(x, 64, 0)), new FakeSlottedStorage(27));
  }

  @Test
  void anEmptyResultFoundNothingAndCostNothing() {
    ContainerScanResult empty = ContainerScanResult.empty();

    assertEquals(0, empty.containerCount());
    assertEquals(List.of(), empty.providers());
    assertFalse(empty.skippedUnloadedChunks());
    assertFalse(empty.exceededBudget());
  }

  @Test
  void theContainerCountFollowsTheProvidersFound() {
    ContainerScanResult result =
        new ContainerScanResult(List.of(providerAt(1), providerAt(2)), 4, 0, 0L);

    assertEquals(2, result.containerCount());
  }

  @Test
  void theProviderListCannotBeMutatedThroughTheResult() {
    ContainerScanResult result = new ContainerScanResult(List.of(providerAt(1)), 1, 0, 0L);

    assertThrows(UnsupportedOperationException.class, () -> result.providers().add(providerAt(2)));
  }

  @Test
  void theProviderListIsDetachedFromTheCallersList() {
    List<StorageProvider> caller = new ArrayList<>();
    caller.add(providerAt(1));
    ContainerScanResult result = new ContainerScanResult(caller, 1, 0, 0L);

    caller.add(providerAt(2));

    assertEquals(1, result.containerCount());
  }

  @ParameterizedTest
  @CsvSource({"0, false", "1, true", "5, true"})
  void skippedChunksAreReportedSoUnloadedGapsAreVisible(int skipped, boolean expected) {
    assertEquals(
        expected, new ContainerScanResult(List.of(), 2, skipped, 0L).skippedUnloadedChunks());
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 1L, 9_999_999L, 10_000_000L})
  void aScanInsideTheBudgetPassesQuietly(long elapsedNanos) {
    assertFalse(new ContainerScanResult(List.of(), 1, 0, elapsedNanos).exceededBudget());
  }

  @ParameterizedTest
  @ValueSource(longs = {10_000_001L, 50_000_000L})
  void aScanOverTheBudgetIsFlagged(long elapsedNanos) {
    assertTrue(new ContainerScanResult(List.of(), 1, 0, elapsedNanos).exceededBudget());
  }

  @Test
  void theBudgetIsTenMilliseconds() {
    assertEquals(Duration.ofMillis(10).toNanos(), ContainerScanResult.BUDGET_NANOS);
  }

  @Test
  void aMissingProviderListIsRejected() {
    assertThrows(NullPointerException.class, () -> new ContainerScanResult(null, 0, 0, 0L));
  }
}
