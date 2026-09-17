package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.text.CountedNoun;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

  public static Text position(ContainerRoleEntry entry) {
    return TerminalScreenText.of(
        "settings.container.at",
        entry.container().getX() + " " + entry.container().getY() + " " + entry.container().getZ());
  }

  public static Text role(ContainerRole role) {
    return TerminalScreenText.of("role." + (role == null ? ContainerRole.DEFAULT : role).id());
  }

  public static int roleColour(ContainerRole role) {
    return switch (role == null ? ContainerRole.DEFAULT : role) {
      case GENERAL, FILTERED -> VanillaPalette.LABEL;
      case EXCLUDED -> VanillaPalette.ACCENT;
    };
  }
}
