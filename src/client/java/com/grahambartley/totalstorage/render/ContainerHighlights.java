package com.grahambartley.totalstorage.render;

import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class ContainerHighlights {
  private static List<HighlightedContainer> containers = List.of();
  private static int ticksLeft;
  private static boolean located;

  private ContainerHighlights() {}

  public static void show(List<HighlightedContainer> shown, int ticks, boolean isLocated) {
    containers = shown == null ? List.of() : List.copyOf(shown);
    ticksLeft = containers.isEmpty() ? 0 : Math.max(0, ticks);
    located = isLocated;
  }

  public static void tick() {
    if (ticksLeft > 0 && --ticksLeft == 0) {
      containers = List.of();
    }
  }

  public static List<HighlightedContainer> showing() {
    return ticksLeft > 0 ? containers : List.of();
  }

  public static boolean isShowing() {
    return ticksLeft > 0 && !containers.isEmpty();
  }

  public static boolean showsLocated() {
    return located && isShowing();
  }

  public static boolean isLocated(BlockPos container) {
    return showsLocated()
        && containers.stream().anyMatch(shown -> shown.container().equals(container));
  }

  public static void clear() {
    containers = List.of();
    ticksLeft = 0;
  }
}
