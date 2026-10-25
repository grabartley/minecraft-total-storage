package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import net.minecraft.text.Text;

public final class FilterColumnText {
  private FilterColumnText() {}

  public static Text nameOf(NetworkTagEntry tag) {
    return tag.immutable() ? Text.translatable(tag.displayName()) : Text.literal(tag.displayName());
  }

  public static Text mode(TagCombinationMode mode) {
    return TerminalScreenText.of(
        mode == TagCombinationMode.AND ? "filter.mode.and" : "filter.mode.or");
  }

  public static String count(NetworkTagEntry tag) {
    return ItemCountFormat.cell(tag.matchingItems());
  }
}
