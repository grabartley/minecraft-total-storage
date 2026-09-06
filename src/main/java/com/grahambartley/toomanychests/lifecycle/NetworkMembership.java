package com.grahambartley.toomanychests.lifecycle;

import com.grahambartley.toomanychests.index.NetworkIndex;
import com.grahambartley.toomanychests.index.NetworkIndexSession;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.ContainerRoleEviction;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Which containers a terminal's network currently holds, kept true as the world changes under it.
 *
 * <p>The session keeps the contents of its containers current; this keeps the set of containers
 * itself current, which is the half a mutation can never report. A broken chest never marks itself
 * dirty, and an unloaded chunk announces nothing at all, so both have to be heard from the world.
 *
 * <p>Departures are immediate and arrivals are not. A container that has gone is dropped the moment
 * it goes, because a listing that offers what is not there is the duplication bug ADR 0002 exists
 * to prevent; a container that has arrived is only picked up when its chunk loads, which is the one
 * moment a re-scan is both cheap and necessary.
 */
public final class NetworkMembership implements ContainerLifecycleListener {
  private final ServerWorld world;
  private final ScanRegion region;
  private final NetworkIndexSession<ItemVariant> session;
  private final ContainerDiscoveryScan discovery;
  private final ContainerLifecycleRegistry lifecycle;
  private final ContainerRoleEviction roles;

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

  /** Powering on: one discovery scan, and from then on the world reports its own changes. */
  public void powerOn() {
    session.begin(discovery.scan(world, region));
    lifecycle.subscribe(this);
  }

  public void powerOff() {
    lifecycle.unsubscribe(this);
    session.end();
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

  public List<StorageOrigin> containers() {
    return session.watchedContainers();
  }

  /**
   * A container that has gone leaves the network and takes the role it was given with it, so the
   * next container placed there starts as a container nobody has vouched for.
   */
  @Override
  public void containerRemoved(StorageOrigin origin) {
    session.drop(origin);
    roles.containerRemoved(origin);
  }

  @Override
  public void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    session.dropChunk(worldKey, chunkPos);
  }

  /**
   * A chunk arriving is the only event that can add containers, and the only one whose containers
   * cannot be named without looking, so it re-scans rather than guessing. Re-scanning also
   * collapses double chests the same way the first scan did, which admitting one block entity at a
   * time could not.
   */
  @Override
  public void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    if (!session.isActive() || !world.getRegistryKey().equals(worldKey)) {
      return;
    }
    if (!region.touchesChunk(chunkPos)) {
      return;
    }
    session.reconcile(discovery.scan(world, region));
  }
}
