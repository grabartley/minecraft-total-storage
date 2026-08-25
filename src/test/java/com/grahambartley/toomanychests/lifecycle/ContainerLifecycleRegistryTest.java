package com.grahambartley.toomanychests.lifecycle;

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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class ContainerLifecycleRegistryTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final BlockPos POS = new BlockPos(8, 64, 8);
  private static final ChunkPos CHUNK = new ChunkPos(0, 0);

  private final ContainerLifecycleRegistry registry = new ContainerLifecycleRegistry();
  private final RecordingListener listener = new RecordingListener();

  @Test
  void aRegistryNobodySubscribedToIsIdle() {
    assertTrue(registry.isIdle());
    assertEquals(0, registry.listenerCount());
    assertFalse(registry.isSubscribed(listener));
  }

  @Test
  void everyEventPublishedToAnEmptyRegistryIsDroppedWithoutWork() {
    registry.containerRemoved(OVERWORLD, POS);
    registry.chunkUnloaded(OVERWORLD, CHUNK);
    registry.chunkLoaded(OVERWORLD, CHUNK);

    assertTrue(registry.isIdle());
  }

  @Test
  void aRemovalIsDeliveredAsTheOriginOfTheContainerThatWent() {
    registry.subscribe(listener);

    registry.containerRemoved(OVERWORLD, POS);

    assertEquals(List.of(new BlockStorageOrigin(OVERWORLD, POS)), listener.removed);
  }

  @Test
  void aChunkUnloadIsDeliveredToEverySubscriber() {
    RecordingListener other = new RecordingListener();
    registry.subscribe(listener);
    registry.subscribe(other);

    registry.chunkUnloaded(OVERWORLD, CHUNK);

    assertEquals(List.of(CHUNK), listener.unloaded);
    assertEquals(List.of(CHUNK), other.unloaded);
  }

  @Test
  void aChunkLoadIsDeliveredToEverySubscriber() {
    registry.subscribe(listener);

    registry.chunkLoaded(OVERWORLD, CHUNK);

    assertEquals(List.of(CHUNK), listener.loaded);
    assertTrue(listener.unloaded.isEmpty());
  }

  @Test
  void subscribingTwiceStillDeliversOnce() {
    registry.subscribe(listener);
    registry.subscribe(listener);

    registry.chunkLoaded(OVERWORLD, CHUNK);

    assertEquals(1, registry.listenerCount());
    assertEquals(List.of(CHUNK), listener.loaded);
  }

  @Test
  void unsubscribingStopsDelivery() {
    registry.subscribe(listener);
    registry.unsubscribe(listener);

    registry.containerRemoved(OVERWORLD, POS);

    assertTrue(listener.removed.isEmpty());
    assertTrue(registry.isIdle());
  }

  @Test
  void unsubscribingSomethingNeverSubscribedIsIgnored() {
    registry.unsubscribe(listener);

    assertTrue(registry.isIdle());
  }

  @Test
  void aListenerThatUnsubscribesWhileBeingToldStillLeavesTheOthersTold() {
    RecordingListener other = new RecordingListener();
    ContainerLifecycleListener quitter =
        new ContainerLifecycleListener() {
          @Override
          public void containerRemoved(StorageOrigin origin) {
            registry.unsubscribe(this);
          }

          @Override
          public void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {}

          @Override
          public void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {}
        };
    registry.subscribe(quitter);
    registry.subscribe(other);

    registry.containerRemoved(OVERWORLD, POS);

    assertEquals(1, registry.listenerCount());
    assertEquals(1, other.removed.size());
  }

  @Test
  void clearingDropsEverySubscriber() {
    registry.subscribe(listener);

    registry.clear();

    assertTrue(registry.isIdle());
  }

  @Test
  void anEventWithNoWorldOrPositionIsIgnoredRatherThanDelivered() {
    registry.subscribe(listener);

    registry.containerRemoved(null, POS);
    registry.containerRemoved(OVERWORLD, null);
    registry.chunkUnloaded(null, CHUNK);
    registry.chunkUnloaded(OVERWORLD, null);
    registry.chunkLoaded(null, CHUNK);
    registry.chunkLoaded(OVERWORLD, null);

    assertTrue(listener.removed.isEmpty());
    assertTrue(listener.unloaded.isEmpty());
    assertTrue(listener.loaded.isEmpty());
  }

  @Test
  void subscribingNothingIsRejected() {
    assertThrows(NullPointerException.class, () -> registry.subscribe(null));
  }

  private static final class RecordingListener implements ContainerLifecycleListener {
    private final List<StorageOrigin> removed = new ArrayList<>();
    private final List<ChunkPos> unloaded = new ArrayList<>();
    private final List<ChunkPos> loaded = new ArrayList<>();

    @Override
    public void containerRemoved(StorageOrigin origin) {
      removed.add(origin);
    }

    @Override
    public void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
      unloaded.add(chunkPos);
    }

    @Override
    public void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
      loaded.add(chunkPos);
    }
  }
}
