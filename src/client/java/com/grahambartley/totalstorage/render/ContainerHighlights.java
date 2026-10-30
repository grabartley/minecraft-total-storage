package com.grahambartley.totalstorage.render;

import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class ContainerHighlights {
  private static List<HighlightedContainer> containers = List.of();
  private static int ticksLeft;
  private static boolean throughBlocks;

  private ContainerHighlights() {}

  public static void show(List<HighlightedContainer> shown, int ticks, boolean overBlocks) {
    containers = shown == null ? List.of() : List.copyOf(shown);
    ticksLeft = containers.isEmpty() ? 0 : Math.max(0, ticks);
    throughBlocks = overBlocks;
  }

  public static void tick() {
    if (ticksLeft > 0 && --ticksLeft == 0) {
      containers = List.of();
      throughBlocks = false;
    }
  }

  public static List<HighlightedContainer> showing() {
    return ticksLeft > 0 ? containers : List.of();
  }

  public static boolean isShowing() {
    return ticksLeft > 0 && !containers.isEmpty();
  }

  public static boolean showsThroughBlocks() {
    return throughBlocks && isShowing();
  }

  public static boolean isShownThroughBlocks(BlockPos container) {
    return showsThroughBlocks()
        && containers.stream().anyMatch(shown -> shown.container().equals(container));
  }

  public static void clear() {
    containers = List.of();
    ticksLeft = 0;
    throughBlocks = false;
  }
}
