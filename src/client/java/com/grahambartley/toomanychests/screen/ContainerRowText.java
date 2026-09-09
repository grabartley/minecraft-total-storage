package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import net.minecraft.text.Text;

public final class ContainerRowText {
  private static final int GENERAL_COLOUR = 0xE4E9F2;
  private static final int FILTERED_COLOUR = 0x9CF0C2;
  private static final int EXCLUDED_COLOUR = 0xED6353;

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
      case GENERAL -> GENERAL_COLOUR;
      case FILTERED -> FILTERED_COLOUR;
      case EXCLUDED -> EXCLUDED_COLOUR;
    };
  }
}
