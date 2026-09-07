package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.network.ServerPlayerEntity;

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
 * <p>Every ending tells the player something, and every ending that moved less than it was asked
 * for also puts their screen back in step, because the reason it moved less is almost always that
 * their listing had gone stale.
 */
public final class WithdrawService {
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
    if (from.isEmpty()) {
      return WithdrawOutcome.nothingAvailable(wanted);
    }
    final Storage<ItemVariant> to = destinationFor(context, payload.destination());
    final OperationResult<Long> result =
        NetworkTransactions.runner()
            .run(
                transaction -> {
                  final long moved =
                      StorageTransfers.gather(from, to, variant, wanted, transaction);
                  return moved > 0L
                      ? OperationResult.committed(moved)
                      : OperationResult.rolledBack("nothing could be taken");
                });
    if (result.rolledBack()) {
      return WithdrawOutcome.noRoom(wanted);
    }
    return WithdrawOutcome.moved(wanted, result.orElse(0L));
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
