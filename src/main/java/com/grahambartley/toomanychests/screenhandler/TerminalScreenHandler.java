package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.ModScreenHandlers;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.listing.TerminalListingState;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Objects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class TerminalScreenHandler extends ScreenHandler {
  private static final double REACH = 4.0;

  private final TerminalOpenData data;
  private final TerminalListingState listing = new TerminalListingState();
  private final TerminalSettingsState settings = new TerminalSettingsState();
  private final TerminalTagState tagPanel = new TerminalTagState();

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

  public TerminalListingState listing() {
    return listing;
  }

  public TerminalSettingsState settings() {
    return settings;
  }

  public TerminalTagState tags() {
    return tagPanel;
  }

  public int radius() {
    return data.radius();
  }

  public TerminalPermission permission() {
    return data.permission();
  }

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
