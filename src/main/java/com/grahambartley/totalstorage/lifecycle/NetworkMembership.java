package com.grahambartley.totalstorage.lifecycle;

import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexSession;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerDiscoveryScan;
import com.grahambartley.totalstorage.storage.ContainerScanResult;
import com.grahambartley.totalstorage.storage.ScanRegion;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.ContainerRoleEviction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public final class NetworkMembership implements ContainerLifecycleListener {
  private final ServerWorld world;
  private final ScanRegion region;
  private final NetworkIndexSession<ItemVariant> session;
  private final ContainerDiscoveryScan discovery;
  private final ContainerLifecycleRegistry lifecycle;
  private final ContainerRoleEviction roles;
  private final ContainerShapes shapes = new ContainerShapes();

  public NetworkMembership(
      ServerWorld world,
      ScanRegion region,
      NetworkIndexSession<ItemVariant> session,
      ContainerDiscoveryScan discovery,
      ContainerLifecycleRegistry lifecycle) {
    this.world = Objects.requireNonNull(world, "world");
    this.region = Objects.requireNonNull(region, "region");
    this.session = Objects.requireNonNull(session, "session");
    this.discovery = Objects.requireNonNull(discovery, "discovery");
    this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    this.roles = new ContainerRoleEviction(this.world, this.region.center());
  }

  public static NetworkMembership around(ServerWorld world, ScanRegion region) {
    return new NetworkMembership(
        world,
        region,
        NetworkIndexSession.forWorld(world),
        new ContainerDiscoveryScan(),
        ContainerLifecycle.registry());
  }

  public void powerOn() {
    final ContainerScanResult scan = discovery.scan(world, region);
    shapes.remember(scan);
    session.begin(scan);
    lifecycle.subscribe(this);
  }

  public void powerOff() {
    lifecycle.unsubscribe(this);
    session.end();
    shapes.clear();
  }

  public NetworkIndex<ItemVariant> index() {
    return session.index();
  }

  public NetworkIndexSession<ItemVariant> session() {
    return session;
  }

  public ScanRegion region() {
    return region;
  }

  public boolean reaches(BlockPos container) {
    return containers().contains(BlockStorageOrigin.of(world, container));
  }

  public Map<BlockPos, StorageOrigin> containersByPosition() {
    final Map<BlockPos, StorageOrigin> byPosition = new LinkedHashMap<>();
    for (final StorageOrigin origin : containers()) {
      if (origin instanceof BlockStorageOrigin block) {
        byPosition.put(block.pos(), origin);
      }
    }
    return byPosition;
  }

  public List<StorageOrigin> containers() {
    return session.watchedContainers();
  }

  @Override
  public void containerRemoved(StorageOrigin origin) {
    session.drop(origin);
    roles.containerRemoved(origin);
  }

  @Override
  public void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    session.dropChunk(worldKey, chunkPos);
  }

  @Override
  public void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    rescanIfReached(worldKey, chunkPos);
  }

  @Override
  public void containerPlaced(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    rescanIfReached(worldKey, chunkPos);
  }

  private void rescanIfReached(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    if (!session.isActive() || !world.getRegistryKey().equals(worldKey)) {
      return;
    }
    if (!region.touchesChunk(chunkPos)) {
      return;
    }
    final ContainerScanResult scan = discovery.scan(world, region);
    session.reconcile(scan);
    shapes.reshaped(scan).forEach(session::containerChanged);
  }
}
