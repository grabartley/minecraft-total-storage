package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HighlightSegmentTest {
  private static final double TOLERANCE = 1.0e-9;

  static Stream<Arguments> segments() {
    return Stream.of(
        Arguments.of(new Vec3d(0, 0, 0), new Vec3d(2, 0, 0), new Vec3d(1, 0, 0)),
        Arguments.of(new Vec3d(5, 3, -2), new Vec3d(5, -1, -2), new Vec3d(0, -1, 0)),
        Arguments.of(
            new Vec3d(0, 0, 0),
            new Vec3d(1, 1, 0),
            new Vec3d(0.7071067811865476, 0.7071067811865476, 0)),
        Arguments.of(
            new Vec3d(1, 0, 1),
            new Vec3d(0, 1, 0),
            new Vec3d(-0.5773502691896258, 0.5773502691896258, -0.5773502691896258)));
  }

  @ParameterizedTest
  @MethodSource("segments")
  void theNormalIsAUnitVectorPointingFromTheStartToTheEnd(Vec3d from, Vec3d to, Vec3d expected) {
    final Vec3d normal = new HighlightSegment(from, to).normal();

    assertEquals(expected.x, normal.x, TOLERANCE);
    assertEquals(expected.y, normal.y, TOLERANCE);
    assertEquals(expected.z, normal.z, TOLERANCE);
  }

  @Test
  void aSegmentCannotStartAndEndAtTheSamePoint() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new HighlightSegment(new Vec3d(1, 2, 3), new Vec3d(1, 2, 3)));
  }
}
