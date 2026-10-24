package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ListingTooltip {
  private ListingTooltip() {}

  public static <V> List<Text> extraLines(ListingRow<V> row) {
    Objects.requireNonNull(row, "row");
    final List<Text> lines = new ArrayList<>(2);
    lines.add(
        TerminalScreenText.of("total", ItemCountFormat.exact(row.count()))
            .formatted(Formatting.GRAY));
    if (row.isSpreadAcrossContainers()) {
      lines.add(
          TerminalScreenText.of("containers", row.providerCount()).formatted(Formatting.DARK_GRAY));
    }
    return List.copyOf(lines);
  }
}
