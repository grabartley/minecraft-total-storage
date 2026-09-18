package com.grahambartley.toomanychests.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HighlightColourTest {
  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleIsDrawnFullyOpaque(ContainerRole role) {
    assertEquals(1.0f, HighlightColour.alpha(HighlightColour.of(role)));
  }

  @Test
  void noTwoRolesShareAColour() {
    assertNotEquals(
        HighlightColour.of(ContainerRole.GENERAL), HighlightColour.of(ContainerRole.FILTERED));
    assertNotEquals(
        HighlightColour.of(ContainerRole.FILTERED), HighlightColour.of(ContainerRole.EXCLUDED));
    assertNotEquals(
        HighlightColour.of(ContainerRole.GENERAL), HighlightColour.of(ContainerRole.EXCLUDED));
  }

  @Test
  void everyRoleIsToldApartByShapeAndNotOnlyByColour() {
    assertEquals(HighlightShape.SOLID, HighlightColour.shapeOf(ContainerRole.GENERAL));
    assertEquals(HighlightShape.DASHED, HighlightColour.shapeOf(ContainerRole.FILTERED));
    assertEquals(HighlightShape.DOUBLED, HighlightColour.shapeOf(ContainerRole.EXCLUDED));
  }

  @Test
  void noTwoRolesShareAShape() {
    final Set<HighlightShape> shapes = new HashSet<>();
    for (final ContainerRole role : ContainerRole.values()) {
      assertTrue(shapes.add(HighlightColour.shapeOf(role)), role + " reuses a shape");
    }
  }

  @Test
  void aContainerWithNoRoleIsDrawnAsTheDefaultOne() {
    assertEquals(HighlightColour.of(ContainerRole.DEFAULT), HighlightColour.of(null));
    assertEquals(HighlightColour.shapeOf(ContainerRole.DEFAULT), HighlightColour.shapeOf(null));
  }

  @Test
  void theChannelsAreReadBackInTheOrderTheyWerePacked() {
    int colour = 0xFF336699;

    assertEquals(1.0f, HighlightColour.alpha(colour));
    assertEquals(0x33 / 255.0f, HighlightColour.red(colour));
    assertEquals(0x66 / 255.0f, HighlightColour.green(colour));
    assertEquals(0x99 / 255.0f, HighlightColour.blue(colour));
  }
}
