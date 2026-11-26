package com.grahambartley.totalstorage.render;

import java.util.List;
import net.minecraft.util.math.Box;

public final class DoubledBox {
  private static final double GAP = 0.2;

  private DoubledBox() {}

  public static List<Box> of(Box footprint) {
    return List.of(footprint, footprint.expand(GAP));
  }
}
