package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.NetworkCapacityService;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

public final class DepositService {
  private final DepositDestinations destinations;
  private final NetworkCapacityService capacity;

  public DepositService() {
    this(new DepositDestinations(), new NetworkCapacityService());
  }

  public DepositService(DepositDestinations destinations, NetworkCapacityService capacity) {
    this.destinations = Objects.requireNonNull(destinations, "destinations");
    this.capacity = Objects.requireNonNull(capacity, "capacity");
  }

  public DepositOutcome deposit(ServerPlayerEntity player, DepositRequestPayload payload) {
    if (payload == null) {
      return report(player, DepositOutcome.refused());
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return report(player, DepositOutcome.refused());
    }
    return report(player, take(context, payload));
  }

  public DepositOutcome depositAll(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, syncId).orElse(null);
    if (context == null) {
      return report(player, DepositOutcome.refused());
    }
    if (!OperationGuards.allows(player, ExpensiveOperation.DEPOSIT_ALL)) {
      return DepositOutcome.rateLimited();
    }
    return report(
        player,
        give(
            context,
            PlayerSlots.depositable(player, ServerConfigService.current().depositAllSkipsHotbar()),
            ItemStackLimits.MAX_STACK_SIZE));
  }

  private DepositOutcome give(
      TerminalOperationContext context,
      List<? extends SingleSlotStorage<ItemVariant>> sources,
      int perSlot) {
    final List<Offer> offers = offersFrom(sources, perSlot);
    if (offers.isEmpty()) {
      return DepositOutcome.nothingToDeposit();
    }
    final long offered = offers.stream().mapToLong(Offer::amount).sum();
    final OperationResult<Stored> result =
        NetworkMutation.run(context, transaction -> store(context, offers, transaction));
    if (result.rolledBack()) {
      return nothingStored(context, offered, leftoversOf(offers));
    }
    final Stored stored = result.orElse(Stored.nothing());
    return DepositOutcome.stored(offered, stored.moved(), stored.leftovers());
  }

  private DepositOutcome nothingStored(
      TerminalOperationContext context, long offered, List<DepositLeftover> leftovers) {
    if (capacity.freeSlots(context.world(), context.terminal(), ContainerRole.GENERAL) > 0) {
      return DepositOutcome.noFittingSpace(offered, leftovers);
    }
    if (capacity.freeSlots(context.world(), context.terminal(), ContainerRole.FILTERED) > 0) {
      return DepositOutcome.onlyFilteredSpace(offered, leftovers);
    }
    return DepositOutcome.noSpace(offered, leftovers);
  }

  private static List<DepositLeftover> leftoversOf(List<Offer> offers) {
    final Map<ItemVariant, Long> byVariant = new LinkedHashMap<>();
    for (final Offer offer : offers) {
      byVariant.merge(offer.variant(), offer.amount(), Long::sum);
    }
    return listed(byVariant);
  }

  private static List<DepositLeftover> listed(Map<ItemVariant, Long> byVariant) {
    return byVariant.entrySet().stream()
        .filter(entry -> entry.getValue() > 0L)
        .sorted(
            Comparator.comparingLong((Map.Entry<ItemVariant, Long> e) -> e.getValue()).reversed())
        .map(entry -> new DepositLeftover(entry.getKey(), entry.getValue()))
        .toList();
  }

  private OperationResult<Stored> store(
      TerminalOperationContext context, List<Offer> offers, TransactionContext transaction) {
    final Map<ItemVariant, List<Storage<ItemVariant>>> routed =
        destinations.forVariants(
            context.world(),
            context.terminal(),
            context.network(),
            offers.stream().map(Offer::variant).distinct().toList());
    long moved = 0L;
    final Map<ItemVariant, Long> behind = new LinkedHashMap<>();
    for (final Offer offer : offers) {
      final List<Storage<ItemVariant>> targets = routed.getOrDefault(offer.variant(), List.of());
      final long took =
          targets.isEmpty()
              ? 0L
              : StorageTransfers.distribute(
                  offer.source(), targets, offer.variant(), offer.amount(), transaction);
      moved += took;
      if (took < offer.amount()) {
        behind.merge(offer.variant(), offer.amount() - took, Long::sum);
      }
    }
    return moved > 0L
        ? OperationResult.committed(new Stored(moved, listed(behind)))
        : OperationResult.rolledBack("the network had nowhere to put any of it");
  }

  private static List<Offer> offersFrom(
      List<? extends SingleSlotStorage<ItemVariant>> sources, int perSlot) {
    final List<Offer> offers = new ArrayList<>(sources.size());
    for (final SingleSlotStorage<ItemVariant> source : sources) {
      final long held = Math.min(source.getAmount(), perSlot);
      if (!source.isResourceBlank() && held > 0L) {
        offers.add(new Offer(source, source.getResource(), held));
      }
    }
    return List.copyOf(offers);
  }

  private DepositOutcome take(TerminalOperationContext context, DepositRequestPayload payload) {
    if (payload.source() == DepositSource.CURSOR) {
      return give(context, List.of(new CursorStorage(context.handler())), payload.amount());
    }
    if (!LockedSlots.isValidSlot(payload.slot())) {
      return DepositOutcome.refused();
    }
    return give(
        context, List.of(PlayerSlots.slotAt(context.player(), payload.slot())), payload.amount());
  }

  private static DepositOutcome report(ServerPlayerEntity player, DepositOutcome outcome) {
    if (player == null) {
      return outcome;
    }
    OperationMessages.forDeposit(outcome)
        .ifPresent(message -> TerminalNotice.tell(player, message));
    if (outcome.result().movedAnything()) {
      player
          .getWorld()
          .playSound(
              null,
              player.getBlockPos(),
              ModSounds.TERMINAL_DEPOSIT_CONFIRM,
              SoundCategory.BLOCKS,
              0.5f,
              1.0f);
    }
    TerminalResync.of(player);
    return outcome;
  }

  private record Offer(SingleSlotStorage<ItemVariant> source, ItemVariant variant, long amount) {}

  private record Stored(long moved, List<DepositLeftover> leftovers) {
    static Stored nothing() {
      return new Stored(0L, List.of());
    }
  }
}
