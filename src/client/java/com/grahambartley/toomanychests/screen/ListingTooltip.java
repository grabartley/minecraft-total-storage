package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.listing.ListingRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * What the terminal adds to an item's own tooltip.
 *
 * <p>The exact total is always there, because the number on the cell is compacted the moment it
 * stops fitting and a player asking how much they have wants the real figure rather than the one
 * that fitted.
 *
 * <p>How many containers hold it is only there when it is more than one. A line saying a stack is
 * in one chest tells a player nothing they could act on, and repeating it on every cell would bury
 * the rows where the answer is twelve.
 */
public final class ListingTooltip {
  private ListingTooltip() {}

  public static <V> List<Text> extraLines(ListingRow<V> row) {
    Objects.requireNonNull(row, "row");
    final List<Text> lines = new ArrayList<>(2);
    lines.add(
        Text.translatable(key("total"), ItemCountFormat.exact(row.count()))
            .formatted(Formatting.GRAY));
    if (row.isSpreadAcrossContainers()) {
      lines.add(
          Text.translatable(key("containers"), row.providerCount())
              .formatted(Formatting.DARK_GRAY));
    }
    return List.copyOf(lines);
  }

  private static String key(String suffix) {
    return "screen." + TooManyChests.MOD_ID + ".terminal." + suffix;
  }
}
