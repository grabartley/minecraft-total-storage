package com.grahambartley.totalstorage.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CrossedBox {
  private CrossedBox() {}

  public static List<HighlightSegment> of(Box box, Vec3d viewer) {
    final List<HighlightSegment> segments = new ArrayList<>();
    for (final Direction face : Direction.values()) {
      if (faces(box, face, viewer)) {
        final int[] corners = diagonalsOf(face);
        segments.add(segment(box, corners[0], corners[1]));
        segments.add(segment(box, corners[2], corners[3]));
      }
    }
    return List.copyOf(segments);
  }

  private static boolean faces(Box box, Direction face, Vec3d viewer) {
    final Direction.Axis axis = face.getAxis();
    final double eye = viewer.getComponentAlongAxis(axis);
    return face.getDirection() == Direction.AxisDirection.POSITIVE
        ? eye > box.getMax(axis)
        : eye < box.getMin(axis);
  }

  private static int[] diagonalsOf(Direction face) {
    return switch (face) {
      case WEST -> new int[] {0, 3, 1, 2};
      case EAST -> new int[] {4, 7, 5, 6};
      case DOWN -> new int[] {0, 5, 1, 4};
      case UP -> new int[] {2, 7, 3, 6};
      case NORTH -> new int[] {0, 6, 2, 4};
      case SOUTH -> new int[] {1, 7, 3, 5};
    };
  }

  private static HighlightSegment segment(Box box, int from, int to) {
    return new HighlightSegment(corner(box, from), corner(box, to));
  }

  private static Vec3d corner(Box box, int index) {
    return new Vec3d(
        (index & 4) == 0 ? box.minX : box.maxX,
        (index & 2) == 0 ? box.minY : box.maxY,
        (index & 1) == 0 ? box.minZ : box.maxZ);
  }
}
