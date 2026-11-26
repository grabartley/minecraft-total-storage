package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CrossedBoxTest {
  private static final double TOLERANCE = 1.0e-9;

  static Stream<Arguments> footprints() {
    return Stream.of(
        Arguments.of("single block", new Box(0, 0, 0, 1, 1, 1)),
        Arguments.of("double chest along x", new Box(4, 64, -3, 6, 65, -2)),
        Arguments.of("double chest along z", new Box(-10, -60, 7, -9, -59, 9)),
        Arguments.of("swelled block", new Box(1.998, 69.998, 4.998, 3.002, 71.002, 6.002)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("footprints")
  void everyEndSitsOnACornerOfTheBoxSoTheCrossIsTheSameSizeAsTheBox(String name, Box box) {
    for (final HighlightSegment segment : CrossedBox.of(box)) {
      for (final Vec3d end : List.of(segment.from(), segment.to())) {
        assertTrue(isOneOf(end.x, box.minX, box.maxX), name + " end off a corner on x: " + end);
        assertTrue(isOneOf(end.y, box.minY, box.maxY), name + " end off a corner on y: " + end);
        assertTrue(isOneOf(end.z, box.minZ, box.maxZ), name + " end off a corner on z: " + end);
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("footprints")
  void everyFaceCarriesTwoDiagonalsThatCrossAtItsCentre(String name, Box box) {
    final Map<Face, List<HighlightSegment>> byFace = new HashMap<>();
    for (final HighlightSegment segment : CrossedBox.of(box)) {
      byFace.computeIfAbsent(Face.of(segment), face -> new ArrayList<>()).add(segment);
    }

    assertEquals(
        Set.of(
            new Face(Direction.Axis.X, box.minX),
            new Face(Direction.Axis.X, box.maxX),
            new Face(Direction.Axis.Y, box.minY),
            new Face(Direction.Axis.Y, box.maxY),
            new Face(Direction.Axis.Z, box.minZ),
            new Face(Direction.Axis.Z, box.maxZ)),
        byFace.keySet(),
        name);
    for (final List<HighlightSegment> diagonals : byFace.values()) {
      assertEquals(2, diagonals.size(), name);
      final HighlightSegment first = diagonals.get(0);
      final HighlightSegment second = diagonals.get(1);
      assertNotEquals(first.from(), second.from(), name + " draws the same diagonal twice");
      assertNotEquals(first.from(), second.to(), name + " draws the same diagonal twice");
      final Vec3d firstMiddle = midpoint(first);
      final Vec3d secondMiddle = midpoint(second);
      assertEquals(firstMiddle.x, secondMiddle.x, TOLERANCE, name);
      assertEquals(firstMiddle.y, secondMiddle.y, TOLERANCE, name);
      assertEquals(firstMiddle.z, secondMiddle.z, TOLERANCE, name);
    }
  }

  @Test
  void theFrontFaceOfABlockIsCrossedCornerToCorner() {
    final List<HighlightSegment> front =
        CrossedBox.of(new Box(0, 0, 0, 1, 1, 1)).stream()
            .filter(segment -> segment.from().z == 1 && segment.to().z == 1)
            .toList();

    assertEquals(
        List.of(
            new HighlightSegment(new Vec3d(0, 0, 1), new Vec3d(1, 1, 1)),
            new HighlightSegment(new Vec3d(0, 1, 1), new Vec3d(1, 0, 1))),
        front);
  }

  private record Face(Direction.Axis axis, double at) {
    static Face of(HighlightSegment segment) {
      final Vec3d from = segment.from();
      final Vec3d to = segment.to();
      final List<Face> flat = new ArrayList<>();
      for (final Direction.Axis axis : Direction.Axis.values()) {
        if (axis.choose(from.x, from.y, from.z) == axis.choose(to.x, to.y, to.z)) {
          flat.add(new Face(axis, axis.choose(from.x, from.y, from.z)));
        }
      }
      assertEquals(1, flat.size(), "not a face diagonal: " + segment);
      return flat.get(0);
    }
  }

  private static Vec3d midpoint(HighlightSegment segment) {
    return segment.from().add(segment.to()).multiply(0.5);
  }

  private static boolean isOneOf(double value, double min, double max) {
    return Math.abs(value - min) < TOLERANCE || Math.abs(value - max) < TOLERANCE;
  }
}
