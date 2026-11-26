package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DoubledBoxTest {
  private static final double TOLERANCE = 1.0e-9;

  static Stream<Arguments> footprints() {
    return Stream.of(
        Arguments.of(
            "single block", new Box(0, 0, 0, 1, 1, 1), new Box(-0.2, -0.2, -0.2, 1.2, 1.2, 1.2)),
        Arguments.of(
            "double chest along x",
            new Box(4, 64, -3, 6, 65, -2),
            new Box(3.8, 63.8, -3.2, 6.2, 65.2, -1.8)),
        Arguments.of(
            "double chest along z",
            new Box(-10, -60, 7, -9, -59, 9),
            new Box(-10.2, -60.2, 6.8, -8.8, -58.8, 9.2)),
        Arguments.of(
            "swelled block",
            new Box(new BlockPos(2, 70, 5)).expand(0.002),
            new Box(1.798, 69.798, 4.798, 3.202, 71.202, 6.202)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("footprints")
  void theFirstBoxTracesTheContainerItself(String name, Box footprint, Box outer) {
    assertBoxEquals(footprint, DoubledBox.of(footprint).get(0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("footprints")
  void theSecondBoxSitsAFifthOfABlockOutsideTheContainer(String name, Box footprint, Box outer) {
    assertBoxEquals(outer, DoubledBox.of(footprint).get(1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("footprints")
  void noBoxIsDrawnInsideTheContainerWhereItsOwnFacesWouldHideIt(
      String name, Box footprint, Box outer) {
    final List<Box> boxes = DoubledBox.of(footprint);

    assertEquals(2, boxes.size());
    for (final Box box : boxes) {
      assertTrue(box.minX <= footprint.minX + TOLERANCE, name + " box starts inside on x");
      assertTrue(box.minY <= footprint.minY + TOLERANCE, name + " box starts inside on y");
      assertTrue(box.minZ <= footprint.minZ + TOLERANCE, name + " box starts inside on z");
      assertTrue(box.maxX >= footprint.maxX - TOLERANCE, name + " box ends inside on x");
      assertTrue(box.maxY >= footprint.maxY - TOLERANCE, name + " box ends inside on y");
      assertTrue(box.maxZ >= footprint.maxZ - TOLERANCE, name + " box ends inside on z");
    }
  }

  private static void assertBoxEquals(Box expected, Box actual) {
    assertEquals(expected.minX, actual.minX, TOLERANCE);
    assertEquals(expected.minY, actual.minY, TOLERANCE);
    assertEquals(expected.minZ, actual.minZ, TOLERANCE);
    assertEquals(expected.maxX, actual.maxX, TOLERANCE);
    assertEquals(expected.maxY, actual.maxY, TOLERANCE);
    assertEquals(expected.maxZ, actual.maxZ, TOLERANCE);
  }
}
