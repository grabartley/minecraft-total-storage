package com.grahambartley.totalstorage.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class DashedBox {
  private DashedBox() {}

  public static List<HighlightSegment> of(Box box, int dashes) {
    final List<HighlightSegment> segments = new ArrayList<>(12 * dashes);
    for (final double y : List.of(box.minY, box.maxY)) {
      for (final double z : List.of(box.minZ, box.maxZ)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minX, box.maxX, dashes)) {
          segments.add(
              new HighlightSegment(new Vec3d(span.start(), y, z), new Vec3d(span.end(), y, z)));
        }
      }
    }
    for (final double x : List.of(box.minX, box.maxX)) {
      for (final double z : List.of(box.minZ, box.maxZ)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minY, box.maxY, dashes)) {
          segments.add(
              new HighlightSegment(new Vec3d(x, span.start(), z), new Vec3d(x, span.end(), z)));
        }
      }
    }
    for (final double x : List.of(box.minX, box.maxX)) {
      for (final double y : List.of(box.minY, box.maxY)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minZ, box.maxZ, dashes)) {
          segments.add(
              new HighlightSegment(new Vec3d(x, y, span.start()), new Vec3d(x, y, span.end())));
        }
      }
    }
    return List.copyOf(segments);
  }
}
