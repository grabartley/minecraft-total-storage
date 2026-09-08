package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes items out of a network, and is the only path a client request may take.
 *
 * <p>Per ADR 0009 the request names an item and an amount, never a place, so everything positional
 * is worked out here against live containers inside one transaction. A container broken in the same
 * tick therefore grants nothing rather than granting a phantom stack, and two players racing for
 * the last stack resolve as one success and one shortfall.
 *
 * <p>Nothing is ever dropped on the floor. What will not fit is left in the network, which is why
 * the destination is inserted into before anything is extracted; see {@link StorageTransfers}.
 *
 * <p>A request the network cannot meet is an ordinary race rather than a fault, so it is logged at
 * debug and answered with a resync. Raising it any higher would fill an operator's log with the
 * normal consequence of a listing that is always a little behind the network it describes.
 *
 * <p>Every ending tells the player something, and every ending that moved less than it was asked
 * for also puts their screen back in step, because the reason it moved less is almost always that
 * their listing had gone stale.
 */
public final class WithdrawService {
  private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawService.class);

  private final WithdrawSources sources;

  public WithdrawService() {
    this(new WithdrawSources());
  }

  public WithdrawService(WithdrawSources sources) {
    this.sources = Objects.requireNonNull(sources, "sources");
  }

  public WithdrawOutcome withdraw(ServerPlayerEntity player, WithdrawRequestPayload payload) {
    if (payload == null) {
      return report(player, WithdrawOutcome.refused());
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return report(player, WithdrawOutcome.refused());
    }
    return report(player, take(context, payload));
  }

  private WithdrawOutcome take(TerminalOperationContext context, WithdrawRequestPayload payload) {
    final ItemVariant variant = payload.variant();
    final long wanted =
        WithdrawAmount.resolve(
            payload.amount(), variant.toStack().getMaxCount(), payload.destination());
    if (wanted <= 0L) {
      return WithdrawOutcome.nothingAvailable(0L);
    }
    final List<Storage<ItemVariant>> from =
        sources.holding(context.world(), context.network(), variant);
    final WithdrawOutcome outcome =
        from.isEmpty()
            ? WithdrawOutcome.nothingAvailable(wanted)
            : gather(
                context, variant, wanted, destinationFor(context, payload.destination()), from);
    if (outcome.result() == WithdrawResult.NOTHING_AVAILABLE) {
      LOGGER.debug(
          "The network at {} offered {} but held none of it when asked for {}",
          context.terminal(),
          variant,
          wanted);
    }
    return outcome;
  }

  /**
   * The outcome is settled inside the transaction because deciding why nothing moved needs the
   * destination as it stood at that moment, and a rolled back result carries a reason rather than a
   * value.
   *
   * <p>It is seeded rather than left empty because the runner also rolls back an operation that
   * threw, and that path never reaches the settling. The seed is the ending that asks nothing of
   * the player: an operation that threw has already been logged as an error, and telling somebody
   * their inventory is full would send them to fix a slot that was never the problem.
   */
  private WithdrawOutcome gather(
      TerminalOperationContext context,
      ItemVariant variant,
      long wanted,
      Storage<ItemVariant> to,
      List<Storage<ItemVariant>> from) {
    final AtomicReference<WithdrawOutcome> settled =
        new AtomicReference<>(WithdrawOutcome.nothingAvailable(wanted));
    NetworkMutation.run(
        context,
        transaction -> {
          final long moved = StorageTransfers.gather(from, to, variant, wanted, transaction);
          if (moved > 0L) {
            settled.set(WithdrawOutcome.moved(wanted, moved));
            return OperationResult.committed(moved);
          }
          settled.set(WithdrawShortfall.of(to, variant, wanted, transaction));
          return OperationResult.rolledBack("nothing could be taken");
        });
    return settled.get();
  }

  private static Storage<ItemVariant> destinationFor(
      TerminalOperationContext context, WithdrawDestination destination) {
    return destination.holdsOneStackOnly()
        ? new CursorStorage(context.handler())
        : PlayerSlots.unlockedStorage(context.player());
  }

  /**
   * Every ending resyncs, not only the refusals. A withdrawal that succeeded has changed both the
   * cursor and the counts on the grid, and one that did not was almost certainly asked for by a
   * client looking at something out of date.
   */
  private static WithdrawOutcome report(ServerPlayerEntity player, WithdrawOutcome outcome) {
    if (player == null) {
      return outcome;
    }
    OperationMessages.forWithdrawal(outcome)
        .ifPresent(message -> player.sendMessage(message, true));
    TerminalResync.of(player);
    return outcome;
  }
}
