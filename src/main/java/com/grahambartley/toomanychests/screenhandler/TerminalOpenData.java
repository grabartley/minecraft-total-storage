package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

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
