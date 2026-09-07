package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.ArrayList;
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

  /** The player's whole inventory, minus everything a lock or a role puts out of reach. */
  public DepositOutcome depositAll(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, syncId).orElse(null);
    if (context == null) {
      return report(player, DepositOutcome.refused());
    }
    return report(
        player, give(context, PlayerSlots.unlocked(player), ItemStackLimits.MAX_STACK_SIZE));
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
    final OperationResult<Long> result =
        NetworkTransactions.runner().run(transaction -> store(context, offers, transaction));
    if (result.rolledBack()) {
      return DepositOutcome.noSpace(offered);
    }
    return DepositOutcome.stored(offered, result.orElse(0L));
  }

  /**
   * Routing is worked out for every variant at once, before anything moves, so emptying a full
   * inventory reads the terminal and its containers once rather than once per kind of item.
   */
  private OperationResult<Long> store(
      TerminalOperationContext context, List<Offer> offers, TransactionContext transaction) {
    final Map<ItemVariant, List<Storage<ItemVariant>>> routed =
        destinations.forVariants(
            context.world(),
            context.terminal(),
            context.network(),
            offers.stream().map(Offer::variant).distinct().toList());
    long moved = 0L;
    for (final Offer offer : offers) {
      final List<Storage<ItemVariant>> targets = routed.getOrDefault(offer.variant(), List.of());
      if (targets.isEmpty()) {
        continue;
      }
      moved +=
          StorageTransfers.distribute(
              offer.source(), targets, offer.variant(), offer.amount(), transaction);
    }
    return moved > 0L
        ? OperationResult.committed(moved)
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
    return PlayerSlots.unlockedSlot(context.player(), locks, payload.slot())
        .map(source -> give(context, List.of(source), payload.amount()))
        .orElseGet(DepositOutcome::refused);
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
}
