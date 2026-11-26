package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.Box;
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
    final Map<String, List<HighlightSegment>> byFace = new HashMap<>();
    for (final HighlightSegment segment : CrossedBox.of(box)) {
      byFace.computeIfAbsent(faceOf(segment), face -> new ArrayList<>()).add(segment);
    }

    assertEquals(
        Set.of(
            "x" + box.minX,
            "x" + box.maxX,
            "y" + box.minY,
            "y" + box.maxY,
            "z" + box.minZ,
            "z" + box.maxZ),
        byFace.keySet(),
        name);
    for (final List<HighlightSegment> diagonals : byFace.values()) {
      assertEquals(2, diagonals.size(), name);
      final Vec3d first = midpoint(diagonals.get(0));
      final Vec3d second = midpoint(diagonals.get(1));
      assertEquals(first.x, second.x, TOLERANCE, name);
      assertEquals(first.y, second.y, TOLERANCE, name);
      assertEquals(first.z, second.z, TOLERANCE, name);
      assertTrue(
          !diagonals.get(0).from().equals(diagonals.get(1).from())
              && !diagonals.get(0).from().equals(diagonals.get(1).to()),
          name + " draws the same diagonal twice");
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

  private static String faceOf(HighlightSegment segment) {
    final Vec3d from = segment.from();
    final Vec3d to = segment.to();
    final boolean flatX = from.x == to.x;
    final boolean flatY = from.y == to.y;
    final boolean flatZ = from.z == to.z;
    assertEquals(
        1, (flatX ? 1 : 0) + (flatY ? 1 : 0) + (flatZ ? 1 : 0), "not a face diagonal: " + segment);
    return flatX ? "x" + from.x : flatY ? "y" + from.y : "z" + from.z;
  }

  private static Vec3d midpoint(HighlightSegment segment) {
    return segment.from().add(segment.to()).multiply(0.5);
  }

  private static boolean isOneOf(double value, double min, double max) {
    return Math.abs(value - min) < TOLERANCE || Math.abs(value - max) < TOLERANCE;
  }
}
