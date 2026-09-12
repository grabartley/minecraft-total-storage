package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class QuickStackTargets {
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
    final Map<BlockPos, QuickStackClaim> claimed = claims(world);
    final List<QuickStackTarget> targets = new ArrayList<>();
    for (final StorageProvider provider :
        scan.scan(world, new ScanRegion(centre, radius)).providers()) {
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

  private static Map<BlockPos, QuickStackClaim> claims(ServerWorld world) {
    final Map<BlockPos, QuickStackClaim> claims = new HashMap<>();
    for (final TerminalAddress address : TerminalNetworks.registry().addresses()) {
      if (!address.worldKey().equals(world.getRegistryKey())) {
        continue;
      }
      final NetworkMembership network = TerminalNetworks.registry().find(address).orElse(null);
      final TerminalSettings settings =
          TerminalLookup.settingsAt(world, address.pos()).orElse(null);
      if (network == null || settings == null) {
        continue;
      }
      recordClaims(claims, network, settings);
    }
    return claims;
  }

  private static void recordClaims(
      Map<BlockPos, QuickStackClaim> claims, NetworkMembership network, TerminalSettings settings) {
    final NetworkTagSet tags = settings.tags();
    for (final StorageOrigin origin : network.containers()) {
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      final ContainerAssignment assignment = settings.assignmentFor(block.pos());
      claims.merge(block.pos(), new QuickStackClaim(assignment, tags), QuickStackClaim::strictest);
    }
  }
}
