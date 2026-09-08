package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
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

/**
 * Puts items into a network, and is the only path a client request may take.
 *
 * <p>What is deposited is read from the player's own inventory at the moment of the move rather
 * than taken from the request, so a client cannot claim to be handing over something it does not
 * hold. Where each item lands is {@link DepositRouting}'s decision, evaluated per variant against
 * live containers.
 *
 * <p>Locked slots are never a source. They are absent from {@link PlayerSlots} rather than filtered
 * out later, so a forged deposit-all naming one has nothing to name: the server builds the eligible
 * set itself and the payload carries no slots at all. Armour and the offhand fall out with them.
 *
 * <p>Nothing is ever dropped. Every move inserts before it extracts, so an item the network will
 * not take stays exactly where it was and is reported rather than lost.
 */
public final class DepositService {
  private final DepositDestinations destinations;

  public DepositService() {
    this(new DepositDestinations());
  }

  public DepositService(DepositDestinations destinations) {
    this.destinations = Objects.requireNonNull(destinations, "destinations");
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

  /**
   * The player's whole inventory, minus everything a lock, a role, or the operator's hotbar setting
   * puts out of reach.
   *
   * <p>Rate limited, because it is the one gesture that reads every container in the network at
   * once. The limit is spent only once the request has proven itself real: a packet naming a screen
   * the sender never opened is refused before it can put a legitimate click on cooldown.
   */
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

  /**
   * Moves what the given slots hold, up to {@code perSlot} from each, in one transaction. Storing
   * some of it is a success: the rest stays in the player's inventory and is reported, which is
   * what stops a network filling up from becoming a refusal to store anything at all.
   */
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
        NetworkTransactions.runner().run(transaction -> store(context, offers, transaction));
    if (result.rolledBack()) {
      return DepositOutcome.noSpace(offered, leftoversOf(offers));
    }
    final Stored stored = result.orElse(Stored.nothing());
    return DepositOutcome.stored(offered, stored.moved(), stored.leftovers());
  }

  /**
   * What every offer holds, for the case where the network took none of it.
   *
   * <p>Gathered even though the no-space line does not read it. The outcome has to account for its
   * whole shortfall, and a caller that wants to say which items were turned away has them without
   * asking the inventory again.
   */
  private static List<DepositLeftover> leftoversOf(List<Offer> offers) {
    final Map<ItemVariant, Long> byVariant = new LinkedHashMap<>();
    for (final Offer offer : offers) {
      byVariant.merge(offer.variant(), offer.amount(), Long::sum);
    }
    return listed(byVariant);
  }

  /**
   * Largest first, so a report that has to be shortened keeps the item a player most wants to hear
   * about. Ties keep the order the inventory listed them in, so the same inventory reports the same
   * way twice.
   */
  private static List<DepositLeftover> listed(Map<ItemVariant, Long> byVariant) {
    return byVariant.entrySet().stream()
        .filter(entry -> entry.getValue() > 0L)
        .sorted(
            Comparator.comparingLong((Map.Entry<ItemVariant, Long> e) -> e.getValue()).reversed())
        .map(entry -> new DepositLeftover(entry.getKey(), entry.getValue()))
        .toList();
  }

  /**
   * Routing is worked out for every variant at once, before anything moves, so emptying a full
   * inventory reads the terminal and its containers once rather than once per kind of item.
   */
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

  /**
   * A slot that is locked and a slot that does not exist are answered differently. The first is a
   * lock doing its job and the player is told so; the second could only have come from a forged
   * request, and telling its sender that a slot they invented is locked would be asserting
   * something untrue about the player's inventory.
   */
  private DepositOutcome take(TerminalOperationContext context, DepositRequestPayload payload) {
    if (payload.source() == DepositSource.CURSOR) {
      return give(context, List.of(new CursorStorage(context.handler())), payload.amount());
    }
    if (!LockedSlots.isValidSlot(payload.slot())) {
      return DepositOutcome.refused();
    }
    final LockedSlots locks = LockedSlotsAttachment.get(context.player());
    if (locks.isLocked(payload.slot())) {
      return DepositOutcome.slotLocked();
    }
    return give(
        context, List.of(PlayerSlots.slotAt(context.player(), payload.slot())), payload.amount());
  }

  /**
   * A deposit that stored something says so with a sound rather than a line of text. Items leaving
   * the inventory is the visible half, and a chime is what tells a player it landed somewhere
   * without a message they have to read on every click.
   */
  private static DepositOutcome report(ServerPlayerEntity player, DepositOutcome outcome) {
    if (player == null) {
      return outcome;
    }
    OperationMessages.forDeposit(outcome).ifPresent(message -> player.sendMessage(message, true));
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

  /** One slot's worth of a deposit, read before the transaction so the total can be reported. */
  private record Offer(SingleSlotStorage<ItemVariant> source, ItemVariant variant, long amount) {}

  /** What one transaction managed, and what it could not take, gathered per item. */
  private record Stored(long moved, List<DepositLeftover> leftovers) {
    static Stored nothing() {
      return new Stored(0L, List.of());
    }
  }
}
