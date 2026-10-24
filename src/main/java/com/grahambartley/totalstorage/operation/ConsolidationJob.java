package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.transaction.OperationResult;
import com.grahambartley.totalstorage.transaction.StorageTransfers;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

public final class ConsolidationJob implements NetworkJob {
  private final NetworkOperationContext context;
  private final ConsolidationDestinations destinations;
  private final ContainerSortStep sorting;
  private final LiveStorages live;

  private final Deque<ItemVariant> gathering = new ArrayDeque<>();
  private final Set<StorageOrigin> touched = new LinkedHashSet<>();
  private final Deque<StorageOrigin> tidying = new ArrayDeque<>();
  private final OperationTally tally;

  private boolean gathered;

  public ConsolidationJob(NetworkOperationContext context) {
    this(context, new ConsolidationDestinations(), new ContainerSortStep(), new LiveStorages());
  }

  public ConsolidationJob(
      NetworkOperationContext context,
      ConsolidationDestinations destinations,
      ContainerSortStep sorting,
      LiveStorages live) {
    this.context = Objects.requireNonNull(context, "context");
    this.destinations = Objects.requireNonNull(destinations, "destinations");
    this.sorting = Objects.requireNonNull(sorting, "sorting");
    this.live = Objects.requireNonNull(live, "live");
    this.gathering.addAll(scatteredVariants(context));
    this.tally = new OperationTally(gathering.size() + context.containers().size());
  }

  @Override
  public ExpensiveOperation operation() {
    return ExpensiveOperation.CONSOLIDATE;
  }

  @Override
  public UUID actor() {
    return context.actor();
  }

  @Override
  public boolean hasNothingToDo() {
    return gathering.isEmpty();
  }

  @Override
  public NetworkJobState advance(int budget) {
    if (tally.wasCancelled()) {
      return NetworkJobState.DONE;
    }
    final NetworkMembership network = context.network().orElse(null);
    if (network == null) {
      cancel();
      return NetworkJobState.DONE;
    }
    int spent = 0;
    while (spent < Math.max(1, budget)) {
      final int cost = advanceOneStep(network);
      if (cost == NOTHING_LEFT_TO_DO) {
        tally.finished();
        return NetworkJobState.DONE;
      }
      spent += cost;
    }
    return NetworkJobState.RUNNING;
  }

  private static final int NOTHING_LEFT_TO_DO = -1;

  private int advanceOneStep(NetworkMembership network) {
    if (!gathering.isEmpty()) {
      final Gathered gathered = gather(network, gathering.poll());
      tally.moved(gathered.moved());
      tally.stepped();
      return gathered.containersRead();
    }
    if (!gathered) {
      gathered = true;
      tidying.addAll(touched);
    }
    if (tidying.isEmpty()) {
      return NOTHING_LEFT_TO_DO;
    }
    final var sorted = sorting.sortOne(context, tidying.poll());
    tally.sorted(sorted);
    tally.stepped();
    return sorted.slotsRead();
  }

  private Gathered gather(NetworkMembership network, ItemVariant variant) {
    final List<StorageOrigin> holders = destinations.holdersOf(context, network, variant);
    if (holders.size() < 2) {
      return new Gathered(0L, 1);
    }
    final long held = network.index().countOf(variant);
    final Set<StorageOrigin> filled = new LinkedHashSet<>();
    long moved = 0L;
    int read = 1;
    for (final StorageOrigin destination : destinations.forVariant(context, network, variant)) {
      final List<StorageOrigin> from = new ArrayList<>(holders);
      from.removeAll(filled);
      from.remove(destination);
      if (from.isEmpty()) {
        break;
      }
      read += from.size();
      moved += gatherInto(destination, from, variant, held);
      filled.add(destination);
    }
    return new Gathered(moved, read);
  }

  private long gatherInto(
      StorageOrigin destination, List<StorageOrigin> from, ItemVariant variant, long remaining) {
    final ServerWorld world = context.world();
    final Storage<ItemVariant> into =
        live.of(world, List.of(destination)).stream().findFirst().orElse(null);
    if (into == null) {
      return 0L;
    }
    final List<Storage<ItemVariant>> sources = live.of(world, from);
    if (sources.isEmpty()) {
      return 0L;
    }
    final OperationResult<Long> result =
        NetworkStep.run(
            context.server(),
            withDestination(from, destination),
            context.actor(),
            transaction -> {
              final long taken =
                  StorageTransfers.gather(sources, into, variant, remaining, transaction);
              return taken > 0L
                  ? OperationResult.committed(taken)
                  : OperationResult.rolledBack("nothing of that kind could be gathered");
            });
    final long moved = result.orElse(0L);
    if (moved > 0L) {
      touched.add(destination);
      touched.addAll(from);
    }
    return moved;
  }

  @Override
  public OperationReport report() {
    return tally.report();
  }

  @Override
  public void cancel() {
    tally.cancelled();
    gathering.clear();
    tidying.clear();
  }

  private record Gathered(long moved, int containersRead) {}

  private static List<StorageOrigin> withDestination(
      List<StorageOrigin> sources, StorageOrigin destination) {
    final List<StorageOrigin> watched = new ArrayList<>(sources);
    watched.add(destination);
    return List.copyOf(watched);
  }

  private static List<ItemVariant> scatteredVariants(NetworkOperationContext context) {
    final NetworkMembership network = context.network().orElse(null);
    if (network == null) {
      return List.of();
    }
    final List<ItemVariant> scattered = new ArrayList<>();
    for (final NetworkIndexEntry<ItemVariant> entry : network.index().entries()) {
      if (entry.providerCount() > 1) {
        scattered.add(entry.variant());
      }
    }
    return List.copyOf(scattered);
  }
}
