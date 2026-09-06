package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.ModScreenHandlers;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Objects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * The terminal's half of the client-server channel, and the only slots the terminal really has.
 *
 * <p>The network is not modelled as slots and must not be. A screen handler addresses its contents
 * by index and syncs them one stack at a time, which puts a hard ceiling on how much it can show; a
 * storage network is expected to hold more distinct variants than that ceiling allows. Per ADR 0009
 * the network is instead a paginated, server-resolved listing that no slot index reaches, so the
 * only slots here are the player's own, which deposits need.
 *
 * <p>{@link #canUse} is the recurring gate. Vanilla asks it once per tick for as long as the screen
 * is up, so a terminal that is broken, loses power, or is taken away from a player closes on them
 * within a tick rather than at their next click.
 */
public class TerminalScreenHandler extends ScreenHandler {
  /** The same reach vanilla allows a chest, measured past the block's own bounds. */
  private static final double REACH = 4.0;

  private final TerminalOpenData data;

  public TerminalScreenHandler(int syncId, PlayerInventory playerInventory, TerminalOpenData data) {
    super(ModScreenHandlers.TERMINAL, syncId);
    this.data = Objects.requireNonNull(data, "data");
    for (int index = 0; index < TerminalScreenLayout.SLOT_COUNT; index++) {
      addSlot(
          new Slot(
              playerInventory,
              TerminalScreenLayout.inventoryIndex(index),
              TerminalScreenLayout.x(index),
              TerminalScreenLayout.y(index)));
    }
  }

  public BlockPos terminal() {
    return data.terminal();
  }

  public int radius() {
    return data.radius();
  }

  public TerminalPermission permission() {
    return data.permission();
  }

  public NetworkTagSet tags() {
    return data.tags();
  }

  /**
   * A client is told true because it holds no authority to decide otherwise. Every answer that
   * matters is the server's, and the server is the side whose world is a {@link ServerWorld}.
   *
   * <p>Walking away closes the terminal, as walking away from a chest does. Without it a player
   * could open a terminal, leave, and go on operating every container in the base from anywhere.
   */
  @Override
  public boolean canUse(PlayerEntity player) {
    if (player == null) {
      return false;
    }
    if (!(player.getWorld() instanceof ServerWorld world)) {
      return true;
    }
    return player.canInteractWithBlockAt(data.terminal(), REACH)
        && TerminalOpenGuard.check(world, data.terminal(), player).allowed();
  }

  /**
   * Only the player's own inventory is here, so a shift-click moves a stack between the hotbar and
   * the rest of it. Depositing into the network is a separate operation with its own request, since
   * a network has no slot for an item to be inserted into.
   */
  @Override
  public ItemStack quickMove(PlayerEntity player, int index) {
    final Slot source = slots.get(index);
    if (source == null || !source.hasStack()) {
      return ItemStack.EMPTY;
    }
    final ItemStack moving = source.getStack();
    final ItemStack original = moving.copy();
    final boolean fromHotbar = TerminalScreenLayout.isHotbar(index);
    final int targetStart = fromHotbar ? 0 : TerminalScreenLayout.HOTBAR_START;
    final int targetEnd =
        fromHotbar ? TerminalScreenLayout.HOTBAR_START : TerminalScreenLayout.SLOT_COUNT;
    if (!insertItem(moving, targetStart, targetEnd, false)) {
      return ItemStack.EMPTY;
    }
    if (moving.isEmpty()) {
      source.setStack(ItemStack.EMPTY);
    } else {
      source.markDirty();
    }
    return original;
  }

  /**
   * The other half of {@link TerminalOpener}: a viewer recorded when the screen opened stops being
   * one here, whatever closed it. Vanilla routes closing by hand, by dying and by losing {@link
   * #canUse} through this same call, so there is no path that leaves an entry behind.
   */
  @Override
  public void onClosed(PlayerEntity player) {
    super.onClosed(player);
    if (player == null || !(player.getWorld() instanceof ServerWorld world)) {
      return;
    }
    TerminalAccessControl.viewers()
        .closed(TerminalAddress.of(world, data.terminal()), player.getUuid());
  }
}
