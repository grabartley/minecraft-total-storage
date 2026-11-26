package com.grahambartley.totalstorage.render;

import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class CrossedBox {
  private CrossedBox() {}

  public static List<HighlightSegment> of(Box box) {
    final double x0 = box.minX;
    final double y0 = box.minY;
    final double z0 = box.minZ;
    final double x1 = box.maxX;
    final double y1 = box.maxY;
    final double z1 = box.maxZ;
    return List.of(
        segment(x0, y0, z0, x0, y1, z1),
        segment(x0, y0, z1, x0, y1, z0),
        segment(x1, y0, z0, x1, y1, z1),
        segment(x1, y0, z1, x1, y1, z0),
        segment(x0, y0, z0, x1, y0, z1),
        segment(x0, y0, z1, x1, y0, z0),
        segment(x0, y1, z0, x1, y1, z1),
        segment(x0, y1, z1, x1, y1, z0),
        segment(x0, y0, z0, x1, y1, z0),
        segment(x0, y1, z0, x1, y0, z0),
        segment(x0, y0, z1, x1, y1, z1),
        segment(x0, y1, z1, x1, y0, z1));
  }

  private static HighlightSegment segment(
      double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
    return new HighlightSegment(new Vec3d(fromX, fromY, fromZ), new Vec3d(toX, toY, toZ));
  }
}
