package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import net.minecraft.text.Text;

public final class TagText {
  private TagText() {}

  public static Text row(NetworkTagEntry tag) {
    return tag.assignedItems() == 1
        ? TerminalScreenText.of("tags.row.one", tag.displayName())
        : TerminalScreenText.of("tags.row", tag.displayName(), tag.assignedItems());
  }

  public static Text bulkHint(int matching) {
    return matching == 1
        ? TerminalScreenText.of("tags.assign.bulk.one")
        : TerminalScreenText.of("tags.assign.bulk", matching);
  }
}
