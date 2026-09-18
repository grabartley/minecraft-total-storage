package com.grahambartley.toomanychests.render;

import java.util.List;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public final class DashedBox {
  private DashedBox() {}

  public static void draw(
      MatrixStack matrices, VertexConsumer lines, Box box, int dashes, int colour) {
    final MatrixStack.Entry entry = matrices.peek();
    final float red = HighlightColour.red(colour);
    final float green = HighlightColour.green(colour);
    final float blue = HighlightColour.blue(colour);
    final float alpha = HighlightColour.alpha(colour);
    for (final double y : List.of(box.minY, box.maxY)) {
      for (final double z : List.of(box.minZ, box.maxZ)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minX, box.maxX, dashes)) {
          line(
              entry,
              lines,
              span.start(),
              y,
              z,
              span.end(),
              y,
              z,
              Direction.EAST,
              red,
              green,
              blue,
              alpha);
        }
      }
    }
    for (final double x : List.of(box.minX, box.maxX)) {
      for (final double z : List.of(box.minZ, box.maxZ)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minY, box.maxY, dashes)) {
          line(
              entry,
              lines,
              x,
              span.start(),
              z,
              x,
              span.end(),
              z,
              Direction.UP,
              red,
              green,
              blue,
              alpha);
        }
      }
    }
    for (final double x : List.of(box.minX, box.maxX)) {
      for (final double y : List.of(box.minY, box.maxY)) {
        for (final DashedEdge.Span span : DashedEdge.of(box.minZ, box.maxZ, dashes)) {
          line(
              entry,
              lines,
              x,
              y,
              span.start(),
              x,
              y,
              span.end(),
              Direction.SOUTH,
              red,
              green,
              blue,
              alpha);
        }
      }
    }
  }

  private static void line(
      MatrixStack.Entry entry,
      VertexConsumer lines,
      double fromX,
      double fromY,
      double fromZ,
      double toX,
      double toY,
      double toZ,
      Direction along,
      float red,
      float green,
      float blue,
      float alpha) {
    lines
        .vertex(entry, (float) fromX, (float) fromY, (float) fromZ)
        .color(red, green, blue, alpha)
        .normal(entry, along.getOffsetX(), along.getOffsetY(), along.getOffsetZ());
    lines
        .vertex(entry, (float) toX, (float) toY, (float) toZ)
        .color(red, green, blue, alpha)
        .normal(entry, along.getOffsetX(), along.getOffsetY(), along.getOffsetZ());
  }
}
