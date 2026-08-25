package com.grahambartley.toomanychests.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class ContainerMutationRegistryTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  private static final BlockPos CHEST = new BlockPos(4, 64, 8);
  private static final BlockPos BARREL = new BlockPos(9, 64, 8);

  private final ContainerMutationRegistry registry = new ContainerMutationRegistry();

  @Test
  void aRegistryWithNoWatchersIsIdle() {
    assertTrue(registry.isIdle());
    assertEquals(0, registry.watchedContainerCount());
  }

  @Test
  void aMutationReachesTheListenerWatchingThatContainer() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.publish(OVERWORLD, CHEST);

    assertEquals(List.of(originAt(OVERWORLD, CHEST)), listener.changed);
  }

  @Test
  void aMutationSomewhereElseReachesNobody() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.publish(OVERWORLD, BARREL);

    assertTrue(listener.changed.isEmpty());
  }

  @Test
  void theSamePositionInAnotherDimensionIsADifferentContainer() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.publish(NETHER, CHEST);

    assertTrue(listener.changed.isEmpty());
  }

  @Test
  void everyTerminalWatchingAContainerHearsAboutIt() {
    RecordingListener first = new RecordingListener();
    RecordingListener second = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), first);
    registry.watch(originAt(OVERWORLD, CHEST), second);

    registry.publish(OVERWORLD, CHEST);

    assertEquals(1, first.changed.size());
    assertEquals(1, second.changed.size());
    assertEquals(1, registry.watchedContainerCount());
  }

  @Test
  void watchingTheSameContainerTwiceStillNotifiesOnlyOnce() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.publish(OVERWORLD, CHEST);

    assertEquals(1, listener.changed.size());
  }

  @Test
  void unwatchingStopsNotificationsAndLeavesTheRegistryIdle() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.unwatch(originAt(OVERWORLD, CHEST), listener);
    registry.publish(OVERWORLD, CHEST);

    assertTrue(listener.changed.isEmpty());
    assertTrue(registry.isIdle());
  }

  @Test
  void unwatchingOneTerminalLeavesTheOtherWatching() {
    RecordingListener staying = new RecordingListener();
    RecordingListener leaving = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), staying);
    registry.watch(originAt(OVERWORLD, CHEST), leaving);

    registry.unwatch(originAt(OVERWORLD, CHEST), leaving);
    registry.publish(OVERWORLD, CHEST);

    assertEquals(1, staying.changed.size());
    assertTrue(leaving.changed.isEmpty());
  }

  @Test
  void poweringOffDropsEveryContainerThatTerminalWatched() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);
    registry.watch(originAt(OVERWORLD, BARREL), listener);

    registry.unwatchAll(listener);

    assertTrue(registry.isIdle());
    registry.publish(OVERWORLD, CHEST);
    registry.publish(OVERWORLD, BARREL);
    assertTrue(listener.changed.isEmpty());
  }

  @Test
  void poweringOffOneTerminalLeavesAnotherOnTheSameContainer() {
    RecordingListener staying = new RecordingListener();
    RecordingListener leaving = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), staying);
    registry.watch(originAt(OVERWORLD, CHEST), leaving);

    registry.unwatchAll(leaving);
    registry.publish(OVERWORLD, CHEST);

    assertEquals(1, staying.changed.size());
    assertTrue(registry.isWatched(originAt(OVERWORLD, CHEST)));
  }

  @Test
  void aListenerMayStopWatchingWhileBeingNotified() {
    RecordingListener listener = new RecordingListener();
    listener.onChange = () -> registry.unwatchAll(listener);
    registry.watch(originAt(OVERWORLD, CHEST), listener);

    registry.publish(OVERWORLD, CHEST);

    assertEquals(1, listener.changed.size());
    assertTrue(registry.isIdle());
  }

  @Test
  void unwatchingAContainerNobodyWatchesIsNotAnError() {
    registry.unwatch(originAt(OVERWORLD, CHEST), new RecordingListener());

    assertTrue(registry.isIdle());
  }

  @Test
  void clearingDropsEveryWatcher() {
    RecordingListener listener = new RecordingListener();
    registry.watch(originAt(OVERWORLD, CHEST), listener);
    registry.watch(originAt(NETHER, BARREL), listener);

    registry.clear();

    assertTrue(registry.isIdle());
    assertFalse(registry.isWatched(originAt(OVERWORLD, CHEST)));
  }

  @Test
  void watchingRejectsMissingArguments() {
    assertThrows(NullPointerException.class, () -> registry.watch(null, new RecordingListener()));
    assertThrows(
        NullPointerException.class, () -> registry.watch(originAt(OVERWORLD, CHEST), null));
  }

  private static StorageOrigin originAt(RegistryKey<World> worldKey, BlockPos pos) {
    return new BlockStorageOrigin(worldKey, pos);
  }

  private static final class RecordingListener implements ContainerMutationListener {
    private final List<StorageOrigin> changed = new ArrayList<>();
    private Runnable onChange = () -> {};

    @Override
    public void containerChanged(StorageOrigin origin) {
      changed.add(origin);
      onChange.run();
    }
  }
}
