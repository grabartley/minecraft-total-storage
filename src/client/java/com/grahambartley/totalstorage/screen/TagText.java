package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.terminal.OwnedTerminal;
import net.minecraft.text.Text;

public final class TagText {
  private TagText() {}

  public static Text source(OwnedTerminal source) {
    return source.customTags() == 1
        ? TerminalScreenText.of(
            "tags.copy.row.one", source.pos().getX(), source.pos().getY(), source.pos().getZ())
        : TerminalScreenText.of(
            "tags.copy.row",
            source.pos().getX(),
            source.pos().getY(),
            source.pos().getZ(),
            source.customTags());
  }

  public static Text bulkHint(int matching) {
    return matching == 1
        ? TerminalScreenText.of("tags.assign.bulk.one")
        : TerminalScreenText.of("tags.assign.bulk", matching);
  }
}
