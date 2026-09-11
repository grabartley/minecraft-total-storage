package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import net.minecraft.text.Text;

public final class ContainerRowText {
  private ContainerRowText() {}

  public static Text contents(ContainerRoleEntry entry) {
    if (entry.isEmpty()) {
      return TerminalScreenText.of("settings.container.empty");
    }
    final String amount = ItemCountFormat.cell(entry.itemCount());
    return entry.variantCount() == 1
        ? TerminalScreenText.of("settings.container.one", amount)
        : TerminalScreenText.of("settings.container", entry.variantCount(), amount);
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
