package com.grahambartley.totalstorage.screen;

import java.util.List;
import net.minecraft.item.tooltip.TooltipData;

public record HintTooltipData(List<HintLine> lines) implements TooltipData {
  public HintTooltipData {
    lines = List.copyOf(lines);
  }
}
