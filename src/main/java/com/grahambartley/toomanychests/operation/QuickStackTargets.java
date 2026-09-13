package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.LoadedChunks;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.grahambartley.toomanychests.terminal.TerminalsHolding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class QuickStackTargets {
  private static final TerminalsHolding<NetworkMembership> HOLDING =
      new TerminalsHolding<>(TerminalNetworks.registry(), NetworkMembership::containers);

  private final ContainerDiscoveryScan scan;

  public QuickStackTargets() {
    this(new ContainerDiscoveryScan());
  }

  public QuickStackTargets(ContainerDiscoveryScan scan) {
    this.scan = Objects.requireNonNull(scan, "scan");
  }

  public List<QuickStackTarget> around(ServerWorld world, BlockPos centre, int radius) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(centre, "centre");
    final List<StorageProvider> found =
        scan.scan(world, new ScanRegion(centre, radius)).providers();
    if (found.isEmpty()) {
      return List.of();
    }
    final Map<BlockPos, QuickStackClaim> claimed = claims(world, originsOf(found));
    final List<QuickStackTarget> targets = new ArrayList<>(found.size());
    for (final StorageProvider provider : found) {
      if (!(provider.origin() instanceof BlockStorageOrigin block)) {
        continue;
      }
      final QuickStackClaim claim = claimed.get(block.pos());
      if (claim == null) {
        targets.add(QuickStackTarget.unclaimed(provider));
        continue;
      }
      if (claim.assignment().role() == ContainerRole.EXCLUDED) {
        continue;
      }
      targets.add(new QuickStackTarget(provider, claim.assignment(), claim.tags()));
    }
    return List.copyOf(targets);
  }

  private static List<StorageOrigin> originsOf(List<StorageProvider> found) {
    final List<StorageOrigin> origins = new ArrayList<>(found.size());
    for (final StorageProvider provider : found) {
      origins.add(provider.origin());
    }
    return List.copyOf(origins);
  }

  private static Map<BlockPos, QuickStackClaim> claims(
      ServerWorld world, List<StorageOrigin> nearby) {
    final Map<BlockPos, QuickStackClaim> claims = new HashMap<>();
    for (final TerminalAddress address : HOLDING.any(nearby)) {
      if (!address.worldKey().equals(world.getRegistryKey())
          || !LoadedChunks.holds(world, address.pos())) {
        continue;
      }
      final NetworkMembership network = TerminalNetworks.registry().find(address).orElse(null);
      if (network == null) {
        continue;
      }
      TerminalLookup.settingsAt(world, address.pos())
          .ifPresent(settings -> recordClaims(claims, network, nearby, settings));
    }
    return claims;
  }

  private static void recordClaims(
      Map<BlockPos, QuickStackClaim> claims,
      NetworkMembership network,
      List<StorageOrigin> nearby,
      TerminalSettings settings) {
    final Set<StorageOrigin> held = new HashSet<>(network.containers());
    for (final StorageOrigin origin : nearby) {
      if (!held.contains(origin)) {
        continue;
      }
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      final ContainerAssignment assignment = settings.assignmentFor(block.pos());
      if (assignment.isDefault()) {
        continue;
      }
      claims.merge(
          block.pos(),
          new QuickStackClaim(assignment, settings.tags()),
          QuickStackClaim::strictest);
    }
  }
}
