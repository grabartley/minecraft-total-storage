package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

/**
 * Everything the terminal screen needs the instant it opens, and nothing else.
 *
 * <p>The item list is not here. A network holds far more distinct variants than a screen opening
 * packet could carry, so the list is paginated over its own channel and this payload's size is
 * fixed by the terminal's configuration rather than by how much is stored in it. That is what makes
 * opening a terminal cost the same whether it reaches one chest or a hundred.
 *
 * <p>The reach carried here is the reach the terminal actually has, with the server maximum already
 * applied per ADR 0021, because a screen that reported the configured value would be telling the
 * player about containers the network cannot see.
 *
 * @param terminal where the terminal is, so operations can name the terminal they are aimed at
 * @param radius the effective reach in blocks
 * @param permission what the player who opened it may do
 * @param tags the vocabulary the filter column is drawn from
 */
public record TerminalOpenData(
    BlockPos terminal, int radius, TerminalPermission permission, NetworkTagSet tags) {

  public TerminalOpenData {
    Objects.requireNonNull(terminal, "terminal");
    terminal = terminal.toImmutable();
    radius = Math.max(TerminalSettings.MIN_RADIUS, Math.min(TerminalSettings.MAX_RADIUS, radius));
    permission = permission == null ? TerminalPermission.USE : permission;
    tags = tags == null ? NetworkTagSet.defaults() : tags;
  }

  public boolean mayAdminister() {
    return permission.mayAdminister();
  }
}
