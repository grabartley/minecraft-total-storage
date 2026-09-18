package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class NetworkCapacityWatchTest {
  private static final RegistryKey<World> WORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("too-many-chests", "test"));

  private static final TerminalAddress HERE = new TerminalAddress(WORLD, new BlockPos(1, 2, 3));
  private static final TerminalAddress THERE = new TerminalAddress(WORLD, new BlockPos(9, 2, 3));

  private final NetworkCapacityWatch watch = new NetworkCapacityWatch();

  @Test
  void aNetworkNobodyHasMeasuredYetIsMeasured() {
    assertTrue(watch.needsMeasuring(HERE, 0));
  }

  @Test
  void aNetworkWhoseContentsHaveMovedIsMeasuredAgain() {
    watch.measured(HERE, 4);

    assertTrue(watch.needsMeasuring(HERE, 5));
  }

  @Test
  void aNetworkNothingHasMovedInIsPassedOver() {
    watch.measured(HERE, 4);

    assertFalse(watch.needsMeasuring(HERE, 4));
  }

  @Test
  void eachNetworkIsRememberedSeparately() {
    watch.measured(HERE, 4);

    assertTrue(watch.needsMeasuring(THERE, 4));
    assertFalse(watch.needsMeasuring(HERE, 4));
  }

  @Test
  void aNetworkIsOnlyWatchedOnceItHasBeenMeasured() {
    assertFalse(watch.isWatching(HERE));

    watch.measured(HERE, 4);

    assertTrue(watch.isWatching(HERE));
  }

  @Test
  void aNetworkThatStopsRunningIsForgotten() {
    watch.measured(HERE, 4);
    watch.measured(THERE, 4);

    watch.retainOnly(List.of(THERE));

    assertEquals(1, watch.watchedCount());
    assertFalse(watch.isWatching(HERE));
    assertTrue(watch.needsMeasuring(HERE, 4));
  }

  @Test
  void retainingNothingForgetsEveryNetwork() {
    watch.measured(HERE, 4);

    watch.retainOnly(null);

    assertEquals(0, watch.watchedCount());
  }

  @Test
  void forgettingOneNetworkLeavesTheRestAlone() {
    watch.measured(HERE, 4);
    watch.measured(THERE, 4);

    watch.forget(HERE);

    assertEquals(1, watch.watchedCount());
    assertFalse(watch.needsMeasuring(THERE, 4));
  }

  @Test
  void clearingForgetsEverything() {
    watch.measured(HERE, 4);
    watch.clear();

    assertEquals(0, watch.watchedCount());
  }
}
