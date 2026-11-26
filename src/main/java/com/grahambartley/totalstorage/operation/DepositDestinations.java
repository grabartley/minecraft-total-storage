package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.filter.FilterCandidateFactory;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class DepositDestinations {
  private final StorageProviderResolver resolver;
  private final FilterCandidateFactory candidates;

  public DepositDestinations() {
    this(new StorageProviderResolver(), new FilterCandidateFactory());
  }

  public DepositDestinations(StorageProviderResolver resolver, FilterCandidateFactory candidates) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public DepositPlan plan(ServerWorld world, BlockPos terminal, NetworkMembership network) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(terminal, "terminal");
    Objects.requireNonNull(network, "network");
    return new DepositPlan(
        world,
        terminal,
        TerminalLookup.settingsAt(world, terminal).orElseGet(TerminalSettings::defaults),
        network.containersByPosition(),
        network.index(),
        resolver,
        candidates);
  }
}
