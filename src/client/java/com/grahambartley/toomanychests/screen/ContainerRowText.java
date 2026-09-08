package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import net.minecraft.text.Text;

/**
 * How one container reads in the settings list.
 *
 * <p>A row has to be recognisable as a chest the player has actually stood in front of, and a
 * position alone is not. What is in it usually is: the row that says "3 kinds, 168" is the
 * cobblestone chest, whatever its coordinates happen to be.
 *
 * <p>Roles differ by their word as well as their colour, so the one distinction the panel exists to
 * make survives a player who cannot separate the two greens from the red.
 */
public final class ContainerRowText {
  private static final int GENERAL_COLOUR = 0xE4E9F2;
  private static final int FILTERED_COLOUR = 0x9CF0C2;
  private static final int EXCLUDED_COLOUR = 0xED6353;

  private ContainerRowText() {}

  public static Text position(ContainerRoleEntry entry) {
    final String at =
        entry.container().getX() + " " + entry.container().getY() + " " + entry.container().getZ();
    return entry.isEmpty()
        ? TerminalScreenText.of("settings.container.empty", at)
        : TerminalScreenText.of(
            "settings.container",
            at,
            entry.variantCount(),
            ItemCountFormat.cell(entry.itemCount()));
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
