package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      return WithdrawOutcome.nothingMoved(0L, ShortfallCause.NO_STOCK);
    }
    final List<Storage<ItemVariant>> from =
        sources.holding(context.world(), context.network(), variant);
    final WithdrawOutcome outcome =
        from.isEmpty()
            ? WithdrawOutcome.nothingMoved(wanted, ShortfallCause.NO_STOCK)
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

  private WithdrawOutcome gather(
      TerminalOperationContext context,
      ItemVariant variant,
      long wanted,
      Storage<ItemVariant> to,
      List<Storage<ItemVariant>> from) {
    final WithdrawOutcome[] settled = {
      WithdrawOutcome.nothingMoved(wanted, ShortfallCause.NO_STOCK)
    };
    NetworkMutation.run(
        context,
        transaction -> {
          final long moved = StorageTransfers.gather(from, to, variant, wanted, transaction);
          if (moved >= wanted) {
            settled[0] = WithdrawOutcome.filled(wanted);
            return OperationResult.committed(moved);
          }
          final ShortfallCause cause =
              WithdrawShortfall.causeOf(from, variant, wanted - moved, transaction);
          if (moved > 0L) {
            settled[0] = WithdrawOutcome.partial(wanted, moved, cause);
            return OperationResult.committed(moved);
          }
          settled[0] = WithdrawOutcome.nothingMoved(wanted, cause);
          return OperationResult.rolledBack("nothing could be taken");
        });
    return settled[0];
  }

  private static Storage<ItemVariant> destinationFor(
      TerminalOperationContext context, WithdrawDestination destination) {
    return destination.holdsOneStackOnly()
        ? new CursorStorage(context.handler())
        : PlayerSlots.unlockedStorage(context.player());
  }

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
