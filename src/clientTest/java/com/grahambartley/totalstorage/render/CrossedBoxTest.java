package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
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
  private static final Box BLOCK = new Box(0, 0, 0, 1, 1, 1);

  static Stream<Arguments> views() {
    return Stream.of(
        Arguments.of(
            "block seen straight on from the south",
            BLOCK,
            new Vec3d(0.5, 0.5, 6),
            EnumSet.of(Direction.SOUTH)),
        Arguments.of(
            "block seen from above, south and east",
            BLOCK,
            new Vec3d(3, 2, 4),
            EnumSet.of(Direction.EAST, Direction.UP, Direction.SOUTH)),
        Arguments.of(
            "block seen from below, north and west",
            BLOCK,
            new Vec3d(-2, -1, -3),
            EnumSet.of(Direction.WEST, Direction.DOWN, Direction.NORTH)),
        Arguments.of(
            "double chest along x seen from above and north",
            new Box(4, 64, -3, 6, 65, -2),
            new Vec3d(5, 67, -8),
            EnumSet.of(Direction.UP, Direction.NORTH)),
        Arguments.of(
            "swelled block seen from its east side",
            new Box(1.998, 69.998, 4.998, 3.002, 71.002, 6.002),
            new Vec3d(9, 70.5, 5.5),
            EnumSet.of(Direction.EAST)),
        Arguments.of(
            "viewer inside the box",
            BLOCK,
            new Vec3d(0.5, 0.5, 0.5),
            EnumSet.noneOf(Direction.class)),
        Arguments.of(
            "viewer level with a face is not in front of it",
            BLOCK,
            new Vec3d(1, 0.5, 0.5),
            EnumSet.noneOf(Direction.class)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("views")
  void onlyTheFacesThatPointAtTheViewerAreCrossed(
      String name, Box box, Vec3d viewer, Set<Direction> facing) {
    final Map<Direction, List<HighlightSegment>> byFace = byFace(box, viewer);

    assertEquals(facing, byFace.keySet(), name);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("views")
  void everyEndSitsOnACornerOfTheBoxSoTheCrossIsTheSameSizeAsTheBox(
      String name, Box box, Vec3d viewer, Set<Direction> facing) {
    for (final HighlightSegment segment : CrossedBox.of(box, viewer)) {
      for (final Vec3d end : List.of(segment.from(), segment.to())) {
        assertTrue(isOneOf(end.x, box.minX, box.maxX), name + " end off a corner on x: " + end);
        assertTrue(isOneOf(end.y, box.minY, box.maxY), name + " end off a corner on y: " + end);
        assertTrue(isOneOf(end.z, box.minZ, box.maxZ), name + " end off a corner on z: " + end);
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("views")
  void everyCrossedFaceCarriesTwoDiagonalsThatMeetAtItsCentre(
      String name, Box box, Vec3d viewer, Set<Direction> facing) {
    for (final List<HighlightSegment> diagonals : byFace(box, viewer).values()) {
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
  void theSouthFaceOfABlockIsCrossedCornerToCorner() {
    assertEquals(
        List.of(
            new HighlightSegment(new Vec3d(0, 0, 1), new Vec3d(1, 1, 1)),
            new HighlightSegment(new Vec3d(0, 1, 1), new Vec3d(1, 0, 1))),
        CrossedBox.of(BLOCK, new Vec3d(0.5, 0.5, 6)));
  }

  private static Map<Direction, List<HighlightSegment>> byFace(Box box, Vec3d viewer) {
    final Map<Direction, List<HighlightSegment>> byFace = new EnumMap<>(Direction.class);
    for (final HighlightSegment segment : CrossedBox.of(box, viewer)) {
      byFace.computeIfAbsent(faceOf(box, segment), face -> new ArrayList<>()).add(segment);
    }
    return byFace;
  }

  private static Direction faceOf(Box box, HighlightSegment segment) {
    final List<Direction> flat = new ArrayList<>();
    for (final Direction.Axis axis : Direction.Axis.values()) {
      final double from = segment.from().getComponentAlongAxis(axis);
      if (from == segment.to().getComponentAlongAxis(axis)) {
        flat.add(
            Direction.from(
                axis,
                from == box.getMax(axis)
                    ? Direction.AxisDirection.POSITIVE
                    : Direction.AxisDirection.NEGATIVE));
      }
    }
    assertEquals(1, flat.size(), "not a face diagonal: " + segment);
    return flat.get(0);
  }

  private static Vec3d midpoint(HighlightSegment segment) {
    return segment.from().add(segment.to()).multiply(0.5);
  }

  private static boolean isOneOf(double value, double min, double max) {
    return Math.abs(value - min) < TOLERANCE || Math.abs(value - max) < TOLERANCE;
  }
}
