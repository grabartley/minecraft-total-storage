package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import net.minecraft.text.Text;

public final class FilterColumnText {
  public static final String CUSTOM_MARKER = "• ";

  private FilterColumnText() {}

  public static Text nameOf(NetworkTagEntry tag) {
    return tag.immutable() ? Text.translatable(tag.displayName()) : Text.literal(tag.displayName());
  }

  public static Text columnNameOf(NetworkTagEntry tag) {
    return tag.immutable()
        ? Text.translatable(tag.displayName())
        : Text.literal(CUSTOM_MARKER + tag.id());
  }

  public static Text mode(TagCombinationMode mode) {
    return TerminalScreenText.of(
        mode == TagCombinationMode.AND ? "filter.mode.and" : "filter.mode.or");
  }

  public static Text mode(TagCombinationMode mode, int activeFilters) {
    return activeFilters <= 0
        ? mode(mode)
        : TerminalScreenText.of("filter.mode.active", mode(mode), activeFilters);
  }

  public static String count(NetworkTagEntry tag) {
    return ItemCountFormat.cell(tag.matchingItems());
  }
}
