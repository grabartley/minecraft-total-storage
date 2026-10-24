package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import com.grahambartley.totalstorage.index.NetworkIndexOrdering;
import com.grahambartley.totalstorage.index.NetworkIndexSession;
import com.grahambartley.totalstorage.mutation.ContainerMutationRegistry;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerAssignments;
import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class NetworkCapacityWatchTest {
  private static final RegistryKey<World> WORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("total-storage", "test"));

  private static final TerminalAddress HERE = new TerminalAddress(WORLD, new BlockPos(1, 2, 3));
  private static final TerminalAddress THERE = new TerminalAddress(WORLD, new BlockPos(9, 2, 3));

  private static final Comparator<NetworkIndexEntry<String>> BY_NAME =
      NetworkIndexOrdering.byKey(Function.identity());

  private final NetworkCapacityWatch watch = new NetworkCapacityWatch();

  private static NetworkIndexSession<String> session() {
    return new NetworkIndexSession<>(new ContainerMutationRegistry(), origin -> Map.of(), BY_NAME);
  }

  private static NetworkCapacityStamp stamp(
      NetworkIndexSession<String> session, int invalidations, ContainerAssignments containers) {
    return new NetworkCapacityStamp(session, invalidations, containers);
  }

  private static ContainerAssignments excluding(BlockPos container) {
    return ContainerAssignments.empty()
        .with(container, ContainerAssignment.of(ContainerRole.EXCLUDED));
  }

  @Test
  void aNetworkNobodyHasMeasuredYetIsMeasured() {
    assertTrue(watch.needsMeasuring(HERE, stamp(session(), 0, ContainerAssignments.empty())));
  }

  @Test
  void aNetworkNothingHasMovedInIsPassedOver() {
    NetworkIndexSession<String> session = session();
    watch.measured(HERE, stamp(session, 4, ContainerAssignments.empty()));

    assertFalse(watch.needsMeasuring(HERE, stamp(session, 4, ContainerAssignments.empty())));
  }

  @Test
  void aNetworkWhoseContentsHaveMovedIsMeasuredAgain() {
    NetworkIndexSession<String> session = session();
    watch.measured(HERE, stamp(session, 4, ContainerAssignments.empty()));

    assertTrue(watch.needsMeasuring(HERE, stamp(session, 5, ContainerAssignments.empty())));
  }

  @Test
  void aRoleChangeMovesTheCapacitySoTheNetworkIsMeasuredAgain() {
    NetworkIndexSession<String> session = session();
    watch.measured(HERE, stamp(session, 4, ContainerAssignments.empty()));

    assertTrue(
        watch.needsMeasuring(HERE, stamp(session, 4, excluding(new BlockPos(4, 1, 3)))),
        "An excluded container is no longer room, so the block has to be looked at again");
  }

  @Test
  void aRebuiltNetworkIsMeasuredAgainEvenThoughItsCounterStartsOver() {
    watch.measured(HERE, stamp(session(), 0, ContainerAssignments.empty()));

    assertTrue(
        watch.needsMeasuring(HERE, stamp(session(), 0, ContainerAssignments.empty())),
        "A rescan builds a fresh index whose count starts at zero");
  }

  @Test
  void eachNetworkIsRememberedSeparately() {
    NetworkIndexSession<String> session = session();
    watch.measured(HERE, stamp(session, 4, ContainerAssignments.empty()));

    assertTrue(watch.needsMeasuring(THERE, stamp(session, 4, ContainerAssignments.empty())));
    assertFalse(watch.needsMeasuring(HERE, stamp(session, 4, ContainerAssignments.empty())));
  }

  @Test
  void aNetworkIsOnlyWatchedOnceItHasBeenMeasured() {
    assertFalse(watch.isWatching(HERE));

    watch.measured(HERE, stamp(session(), 4, ContainerAssignments.empty()));

    assertTrue(watch.isWatching(HERE));
  }

  @Test
  void aNetworkThatStopsRunningIsForgotten() {
    watch.measured(HERE, stamp(session(), 4, ContainerAssignments.empty()));
    watch.measured(THERE, stamp(session(), 4, ContainerAssignments.empty()));

    watch.retainOnly(List.of(THERE));

    assertEquals(1, watch.watchedCount());
    assertFalse(watch.isWatching(HERE));
  }

  @Test
  void retainingNothingForgetsEveryNetwork() {
    watch.measured(HERE, stamp(session(), 4, ContainerAssignments.empty()));

    watch.retainOnly(null);

    assertEquals(0, watch.watchedCount());
  }
}
