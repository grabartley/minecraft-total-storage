package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.operation.ExpensiveOperation;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContainerSortService {
  private static final int UNOPENED_SYNC_ID = -1;

  private final SortCandidateFactory candidates;

  public ContainerSortService() {
    this(new SortCandidateFactory());
  }

  public ContainerSortService(SortCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public SortOutcome sortFromButton(ServerPlayerEntity player, int syncId) {
    return ServerConfigService.current().sortButtonEnabled()
        ? sort(player, syncId)
        : SortOutcome.NOT_OFFERED;
  }

  public SortOutcome sort(ServerPlayerEntity player, int syncId) {
    if (player == null) {
      return SortOutcome.NO_OPEN_SCREEN;
    }
    final ScreenHandler handler = player.currentScreenHandler;
    if (handler == null || handler.syncId != syncId) {
      return SortOutcome.NO_OPEN_SCREEN;
    }
    final Optional<SortTarget> target =
        SortableSlots.resolve(handler, player, LockedSlotsAttachment.get(player));
    if (target.isEmpty()) {
      return SortOutcome.SCREEN_NOT_SORTABLE;
    }
    return apply(player, target.get());
  }

  public SortReply sortForCommand(ServerPlayerEntity player) {
    Objects.requireNonNull(player, "player");
    final ScreenHandler open = player.currentScreenHandler;
    if (open != null && open != player.playerScreenHandler) {
      return new SortReply(SortSubject.OPEN_SCREEN, sort(player, open.syncId));
    }
    final BlockPos inView = blockInView(player).orElse(null);
    if (inView != null) {
      final Optional<SortOutcome> outcome = sortBlock(player, inView);
      if (outcome.isPresent()) {
        return new SortReply(SortSubject.CONTAINER_IN_VIEW, outcome.get());
      }
    }
    return new SortReply(SortSubject.INVENTORY, sort(player, player.playerScreenHandler.syncId));
  }

  private static Optional<BlockPos> blockInView(ServerPlayerEntity player) {
    final HitResult hit = player.raycast(player.getBlockInteractionRange(), 1.0f, false);
    return hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult block
        ? Optional.of(block.getBlockPos())
        : Optional.empty();
  }

  private Optional<SortOutcome> sortBlock(ServerPlayerEntity player, BlockPos pos) {
    final World world = player.getWorld();
    final BlockState state = world.getBlockState(pos);
    final NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
    if (factory != null) {
      return Optional.of(sortUnopened(player, factory));
    }
    if (state.getBlock() instanceof EnderChestBlock) {
      return Optional.of(SortOutcome.SCREEN_NOT_SORTABLE);
    }
    return world.getBlockEntity(pos) instanceof Inventory
        ? Optional.of(SortOutcome.NOT_OPENABLE)
        : Optional.empty();
  }

  private SortOutcome sortUnopened(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
    if (player.isSpectator()) {
      return SortOutcome.NOT_OPENABLE;
    }
    final ScreenHandler unopened =
        factory.createMenu(UNOPENED_SYNC_ID, player.getInventory(), player);
    if (unopened == null) {
      return SortOutcome.NOT_OPENABLE;
    }
    try {
      if (!unopened.canUse(player)) {
        return SortOutcome.NOT_OPENABLE;
      }
      return SortableSlots.resolve(unopened, player, LockedSlotsAttachment.get(player))
          .map(target -> apply(player, target))
          .orElse(SortOutcome.SCREEN_NOT_SORTABLE);
    } finally {
      unopened.onClosed(player);
    }
  }

  private SortOutcome apply(ServerPlayerEntity player, SortTarget target) {
    final List<SingleSlotStorage<ItemVariant>> slots = slotsOf(target);
    final SortPlan<ItemVariant> plan =
        SortArrangement.of(slots, target.slotCapacity(), candidates, ConfiguredSortOrder.current());
    if (plan.isEmpty()) {
      return SortOutcome.NOTHING_TO_SORT;
    }
    if (SortArrangement.matches(slots, plan)) {
      return SortOutcome.ALREADY_SORTED;
    }
    if (OperationGuards.refuse(player, ExpensiveOperation.SORT_CONTAINER).isPresent()) {
      return SortOutcome.RATE_LIMITED;
    }
    final OperationResult<Long> result;
    final List<StorageOrigin> changed;
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.overEverything(ContainerMutations.registry())) {
      result =
          NetworkTransactions.runner()
              .run(transaction -> SortArrangement.apply(slots, plan, transaction));
      if (result.committed()) {
        target.inventory().markDirty();
      }
      changed = List.copyOf(witness.changed());
    }
    if (result.rolledBack()) {
      return SortOutcome.FAILED;
    }
    resync(player, target, changed);
    return SortOutcome.SORTED;
  }

  private static void resync(
      ServerPlayerEntity player, SortTarget target, List<StorageOrigin> changed) {
    final List<Inventory> showing =
        new ArrayList<>(ContainerViewerSync.inventoriesOf(player.getServer(), changed));
    showing.add(target.inventory());
    ContainerViewerSync.resyncAll(player.getServer(), showing);
  }

  private static List<SingleSlotStorage<ItemVariant>> slotsOf(SortTarget target) {
    final InventoryStorage storage = InventoryStorage.of(target.inventory(), null);
    final List<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>(target.slotCount());
    for (final int index : target.indices()) {
      slots.add(storage.getSlot(index));
    }
    return slots;
  }
}
