package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ContainerHighlightsTest {
  private static List<HighlightedContainer> one() {
    return List.of(new HighlightedContainer(BlockPos.ORIGIN, ContainerRole.GENERAL));
  }

  @AfterEach
  void stopShowing() {
    ContainerHighlights.clear();
  }

  @Test
  void whatIsShownIsWhatWasSent() {
    ContainerHighlights.show(one(), 5);

    assertTrue(ContainerHighlights.isShowing());
    assertEquals(one(), ContainerHighlights.showing());
  }

  @Test
  void theOutlinesRunOutAfterTheTicksTheyWereGiven() {
    ContainerHighlights.show(one(), 2);

    ContainerHighlights.tick();
    assertTrue(ContainerHighlights.isShowing());

    ContainerHighlights.tick();
    assertFalse(ContainerHighlights.isShowing());
    assertTrue(ContainerHighlights.showing().isEmpty());
  }

  @Test
  void tickingWithNothingShowingChangesNothing() {
    ContainerHighlights.tick();

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void aSecondAnswerReplacesTheFirst() {
    ContainerHighlights.show(one(), 5);

    ContainerHighlights.show(List.of(), 5);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void anEmptyAnswerShowsNothingForNoTimeAtAll() {
    ContainerHighlights.show(List.of(), 100);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void anAnswerOfNothingAtAllIsTreatedAsEmpty() {
    ContainerHighlights.show(null, 100);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void clearingStopsTheOutlinesImmediately() {
    ContainerHighlights.show(one(), 100);

    ContainerHighlights.clear();

    assertFalse(ContainerHighlights.isShowing());
    assertTrue(ContainerHighlights.showing().isEmpty());
  }
}
