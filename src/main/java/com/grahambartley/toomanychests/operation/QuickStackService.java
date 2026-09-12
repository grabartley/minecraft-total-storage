package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.ModSounds;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public final class QuickStackService {
  private final QuickStackTargets targets;
  private final FilterCandidateFactory candidates;

  public QuickStackService() {
    this(new QuickStackTargets(), new FilterCandidateFactory());
  }

  public QuickStackService(QuickStackTargets targets, FilterCandidateFactory candidates) {
    this.targets = Objects.requireNonNull(targets, "targets");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public QuickStackOutcome quickStack(ServerPlayerEntity player) {
    if (player == null || !(player.getWorld() instanceof ServerWorld world)) {
      return QuickStackOutcome.refused();
    }
    if (!OperationGuards.allows(player, ExpensiveOperation.QUICK_STACK)) {
      return QuickStackOutcome.rateLimited();
    }
    final List<QuickStackTarget> nearby =
        targets.around(
            world, player.getBlockPos(), ServerConfigService.current().quickStackRadius());
    final List<SingleSlotStorage<ItemVariant>> sources = PlayerSlots.unlocked(player);
    if (nearby.isEmpty() || sources.isEmpty()) {
      return report(player, QuickStackOutcome.nothingToStack());
    }
    return report(player, stack(player, world, nearby, sources));
  }

  private QuickStackOutcome stack(
      ServerPlayerEntity player,
      ServerWorld world,
      List<QuickStackTarget> nearby,
      List<SingleSlotStorage<ItemVariant>> sources) {
    final Set<StorageOrigin> touched = new LinkedHashSet<>();
    final OperationResult<Long> result =
        NetworkStep.run(
            world.getServer(),
            originsOf(nearby),
            player.getUuid(),
            transaction -> topUp(nearby, sources, touched, transaction));
    final long moved = result.orElse(0L);
    return moved > 0L
        ? QuickStackOutcome.stacked(moved, touched.size())
        : QuickStackOutcome.nothingToStack();
  }

  private OperationResult<Long> topUp(
      List<QuickStackTarget> nearby,
      List<SingleSlotStorage<ItemVariant>> sources,
      Set<StorageOrigin> touched,
      TransactionContext transaction) {
    long moved = 0L;
    for (final SingleSlotStorage<ItemVariant> source : sources) {
      if (source.isResourceBlank() || source.getAmount() <= 0L) {
        continue;
      }
      moved += intoEveryTargetHolding(source, nearby, touched, transaction);
    }
    return moved > 0L
        ? OperationResult.committed(moved)
        : OperationResult.rolledBack("nothing the player carries belongs in a container nearby");
  }

  private long intoEveryTargetHolding(
      SingleSlotStorage<ItemVariant> source,
      List<QuickStackTarget> nearby,
      Set<StorageOrigin> touched,
      TransactionContext transaction) {
    final ItemVariant variant = source.getResource();
    final FilterCandidate candidate = candidates.of(variant);
    long moved = 0L;
    for (final QuickStackTarget target : nearby) {
      final long carried = source.getAmount();
      if (carried <= 0L) {
        break;
      }
      if (!target.accepts(candidate) || !target.holds(variant)) {
        continue;
      }
      final long took =
          StorageTransfers.move(source, target.storage(), variant, carried, transaction);
      if (took > 0L) {
        touched.add(target.origin());
        moved += took;
      }
    }
    return moved;
  }

  private static List<StorageOrigin> originsOf(List<QuickStackTarget> nearby) {
    final List<StorageOrigin> origins = new ArrayList<>(nearby.size());
    for (final QuickStackTarget target : nearby) {
      origins.add(target.origin());
    }
    return List.copyOf(origins);
  }

  private static QuickStackOutcome report(ServerPlayerEntity player, QuickStackOutcome outcome) {
    if (outcome.result() == QuickStackResult.RATE_LIMITED) {
      return outcome;
    }
    player.sendMessage(
        outcome.result().movedAnything()
            ? NetworkOperationMessages.quickStacked(outcome.moved(), outcome.containersTouched())
            : NetworkOperationMessages.nothingToQuickStack(),
        true);
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
    return outcome;
  }
}
