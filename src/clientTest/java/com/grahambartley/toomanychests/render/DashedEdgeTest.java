package com.grahambartley.toomanychests.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DashedEdgeTest {
  private static final double TOLERANCE = 1.0e-9;

  @Test
  void oneDashIsTheWholeEdge() {
    assertEquals(List.of(new DashedEdge.Span(0.0, 1.0)), DashedEdge.of(0.0, 1.0, 1));
  }

  @Test
  void dashesAndGapsAlternateInEqualParts() {
    final List<DashedEdge.Span> spans = DashedEdge.of(0.0, 5.0, 3);

    assertEquals(3, spans.size());
    assertEquals(0.0, spans.get(0).start(), TOLERANCE);
    assertEquals(1.0, spans.get(0).end(), TOLERANCE);
    assertEquals(2.0, spans.get(1).start(), TOLERANCE);
    assertEquals(3.0, spans.get(1).end(), TOLERANCE);
    assertEquals(4.0, spans.get(2).start(), TOLERANCE);
    assertEquals(5.0, spans.get(2).end(), TOLERANCE);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 7})
  void anEdgeAlwaysStartsAndEndsOnADashSoTheCornersStayDrawn(int dashes) {
    final List<DashedEdge.Span> spans = DashedEdge.of(-3.5, 4.5, dashes);

    assertEquals(dashes, spans.size());
    assertEquals(-3.5, spans.get(0).start(), TOLERANCE);
    assertEquals(4.5, spans.get(spans.size() - 1).end(), TOLERANCE);
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 3, 7})
  void noDashEverTouchesTheNextOne(int dashes) {
    final List<DashedEdge.Span> spans = DashedEdge.of(0.0, 1.0, dashes);

    for (int dash = 0; dash + 1 < spans.size(); dash++) {
      assertTrue(
          spans.get(dash).end() < spans.get(dash + 1).start(),
          "dash " + dash + " runs into the one after it");
    }
  }

  @Test
  void anEdgeRunningBackwardsStillDashesFromItsOwnStart() {
    final List<DashedEdge.Span> spans = DashedEdge.of(1.0, 0.0, 2);

    assertEquals(1.0, spans.get(0).start(), TOLERANCE);
    assertEquals(0.0, spans.get(1).end(), TOLERANCE);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void anEdgeCannotCarryFewerThanOneDash(int dashes) {
    assertThrows(IllegalArgumentException.class, () -> DashedEdge.of(0.0, 1.0, dashes));
  }
}
