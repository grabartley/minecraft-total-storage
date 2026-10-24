package com.grahambartley.totalstorage.terminal;

import java.util.Objects;
import net.minecraft.util.math.BlockPos;

public record OwnedTerminal(BlockPos pos, int customTags) {
  public OwnedTerminal {
    Objects.requireNonNull(pos, "pos");
    pos = pos.toImmutable();
    customTags = Math.max(0, customTags);
  }

  public boolean holdsAnythingToCopy() {
    return customTags > 0;
  }
}
