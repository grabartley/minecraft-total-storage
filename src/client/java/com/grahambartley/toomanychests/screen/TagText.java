package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.OwnedTerminal;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class TagText {
  private TagText() {}

  public static Text row(NetworkTagEntry tag) {
    return tag.assignedItems() == 1
        ? TerminalScreenText.of("tags.row.one", tag.displayName())
        : TerminalScreenText.of("tags.row", tag.displayName(), tag.assignedItems());
  }

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

  public static MutableText assignHint(boolean onMac) {
    return TerminalScreenText.of(onMac ? "tags.assign.hint.mac" : "tags.assign.hint");
  }

  public static Text bulkHint(int matching) {
    return matching == 1
        ? TerminalScreenText.of("tags.assign.bulk.one")
        : TerminalScreenText.of("tags.assign.bulk", matching);
  }
}
