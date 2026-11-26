package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DashedBoxTest {
  private static final double TOLERANCE = 1.0e-9;
  private static final Box BOX = new Box(0, 0, 0, 1, 2, 3);

  @ParameterizedTest
  @CsvSource({"1, 12, 24.0", "2, 24, 16.0", "3, 36, 14.4"})
  void everyEdgeIsSplitIntoItsDashes(int dashes, int segments, double drawnLength) {
    final List<HighlightSegment> drawn = DashedBox.of(BOX, dashes);

    assertEquals(segments, drawn.size());
    assertEquals(
        drawnLength,
        drawn.stream().mapToDouble(segment -> segment.from().distanceTo(segment.to())).sum(),
        TOLERANCE);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void everyDashRunsAlongAnEdgeOfTheBox(int dashes) {
    for (final HighlightSegment segment : DashedBox.of(BOX, dashes)) {
      final Vec3d from = segment.from();
      final Vec3d to = segment.to();
      final int fixedOnEdge =
          (from.x == to.x && onEdge(from.x, BOX.minX, BOX.maxX) ? 1 : 0)
              + (from.y == to.y && onEdge(from.y, BOX.minY, BOX.maxY) ? 1 : 0)
              + (from.z == to.z && onEdge(from.z, BOX.minZ, BOX.maxZ) ? 1 : 0);
      assertEquals(2, fixedOnEdge, "dash leaves the box edges: " + segment);
    }
  }

  @Test
  void theBottomFrontEdgeAlongXIsDashedFromCornerToCorner() {
    final List<HighlightSegment> edge =
        DashedBox.of(BOX, 3).stream()
            .filter(segment -> segment.from().y == 0 && segment.to().y == 0)
            .filter(segment -> segment.from().z == 0 && segment.to().z == 0)
            .filter(segment -> segment.from().x != segment.to().x)
            .toList();

    assertEquals(3, edge.size());
    assertSpan(edge.get(0), 0.0, 0.2);
    assertSpan(edge.get(1), 0.4, 0.6);
    assertSpan(edge.get(2), 0.8, 1.0);
    assertTrue(edge.stream().allMatch(segment -> segment.from().x < segment.to().x));
  }

  private static void assertSpan(HighlightSegment segment, double start, double end) {
    assertEquals(start, segment.from().x, TOLERANCE);
    assertEquals(end, segment.to().x, TOLERANCE);
  }

  private static boolean onEdge(double value, double min, double max) {
    return value == min || value == max;
  }
}
