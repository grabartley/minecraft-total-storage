package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.text.CountedNoun;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContainerRowText {
  private ContainerRowText() {}

  public static MutableText contents(ContainerRoleEntry entry) {
    return entry.isEmpty()
        ? TerminalScreenText.of("settings.container.empty")
        : CountedNoun.KINDS.of(entry.variantCount());
  }

  public static MutableText itemTotal(ContainerRoleEntry entry) {
    return CountedNoun.ITEMS.ofFormatted(
        ItemCountFormat.cell(entry.itemCount()), entry.itemCount());
  }

  public static MutableText slotsUsed(ContainerRoleEntry entry) {
    return TerminalScreenText.of("settings.container.slots", entry.usedSlots(), entry.totalSlots());
  }

  public static String coordinates(BlockPos container) {
    return container.getX() + " " + container.getY() + " " + container.getZ();
  }

  public static Text position(ContainerRoleEntry entry) {
    return TerminalScreenText.of("settings.container.at", coordinates(entry.container()));
  }

  public static MutableText identity(ContainerRoleEntry entry) {
    return TerminalScreenText.of(
        "settings.container.identity", coordinates(entry.container()), contents(entry));
  }

  public static MutableText role(ContainerRole role) {
    return TerminalScreenText.of("role." + safe(role).id());
  }

  public static MutableText roleMeaning(ContainerRole role) {
    return TerminalScreenText.of("role." + safe(role).id() + ".meaning");
  }

  public static int roleColour(ContainerRole role) {
    return switch (safe(role)) {
      case GENERAL, FILTERED -> VanillaPalette.LABEL;
      case EXCLUDED -> VanillaPalette.ACCENT;
    };
  }

  private static ContainerRole safe(ContainerRole role) {
    return role == null ? ContainerRole.DEFAULT : role;
  }
}
