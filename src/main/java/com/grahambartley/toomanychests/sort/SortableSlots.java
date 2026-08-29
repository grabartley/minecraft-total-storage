package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.lock.LockedSlots;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

/**
 * Works out which slots of an open screen a sort may rearrange, or refuses the screen.
 *
 * <p>This is the second of the two gates {@link ScreenSortPolicy} describes, and the one that has
 * to hold for screens nobody has named. A slot is only rearrangeable if it will take an arbitrary
 * item and give it back, which is exactly what a machine's input, filter, or output slot will not
 * do. Refusing the whole screen when any one slot fails is deliberate: a partly sortable machine is
 * a machine whose contents are about to end up somewhere they do not belong.
 *
 * <p>Sorting a container never touches the player's own slots, and sorting the player's inventory
 * never touches armour, the offhand, or the crafting grid, because those live outside the lockable
 * range.
 */
public final class SortableSlots {
  private static final int MIN_SLOTS = 2;
  private static final int FULL_STACK = 64;

  private SortableSlots() {}

  public static Optional<SortTarget> resolve(
      ScreenHandler handler, PlayerEntity player, LockedSlots locks) {
    if (handler == null || player == null) {
      return Optional.empty();
    }
    if (handler instanceof PlayerScreenHandler) {
      return playerInventory(player, locks == null ? LockedSlots.empty() : locks);
    }
    return ScreenSortPolicy.allows(typeIdOf(handler))
        ? container(handler, player)
        : Optional.empty();
  }

  /**
   * The registered id of the screen, or null when it has none. {@link ScreenHandler#getType()}
   * throws rather than returning null for a handler built without a type, which the player's own
   * inventory screen is and a modded screen may be.
   */
  private static Identifier typeIdOf(ScreenHandler handler) {
    try {
      return Registries.SCREEN_HANDLER.getId(handler.getType());
    } catch (UnsupportedOperationException noType) {
      return null;
    }
  }

  private static Optional<SortTarget> playerInventory(PlayerEntity player, LockedSlots locks) {
    final PlayerInventory inventory = player.getInventory();
    final List<Integer> indices = new ArrayList<>(LockedSlots.SLOT_COUNT);
    for (int index = LockedSlots.FIRST_SLOT; index <= LockedSlots.LAST_SLOT; index++) {
      if (!locks.isLocked(index)) {
        indices.add(index);
      }
    }
    return indices.size() < MIN_SLOTS
        ? Optional.empty()
        : Optional.of(new SortTarget(inventory, indices));
  }

  private static Optional<SortTarget> container(ScreenHandler handler, PlayerEntity player) {
    Inventory backing = null;
    final List<Integer> indices = new ArrayList<>();
    for (final Slot slot : handler.slots) {
      if (slot.inventory instanceof PlayerInventory) {
        continue;
      }
      if (!isRearrangeable(slot, player)) {
        return Optional.empty();
      }
      if (backing == null) {
        backing = slot.inventory;
      } else if (backing != slot.inventory) {
        return Optional.empty();
      }
      indices.add(slot.getIndex());
    }
    return backing == null || indices.size() < MIN_SLOTS
        ? Optional.empty()
        : Optional.of(new SortTarget(backing, indices));
  }

  /**
   * Two unrelated probes rather than one, because a slot that filters by item accepts plenty of
   * things and only refusing both would prove nothing.
   */
  private static boolean isRearrangeable(Slot slot, PlayerEntity player) {
    return slot.getMaxItemCount() >= FULL_STACK
        && slot.canTakeItems(player)
        && slot.canInsert(new ItemStack(Items.COBBLESTONE))
        && slot.canInsert(new ItemStack(Items.DIAMOND));
  }
}
