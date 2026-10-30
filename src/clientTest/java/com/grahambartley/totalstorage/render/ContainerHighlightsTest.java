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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerHighlightsTest {
  private static List<HighlightedContainer> one() {
    return List.of(new HighlightedContainer(BlockPos.ORIGIN, ContainerRole.GENERAL));
  }

  @ParameterizedTest(name = "located {0}")
  @ValueSource(booleans = {true, false})
  void onlyALocatedContainerShowsThroughBlocks(boolean located) {
    ContainerHighlights.show(one(), 5, located);

    assertEquals(located, ContainerHighlights.showsThroughBlocks());
    assertEquals(located, ContainerHighlights.isShownThroughBlocks(BlockPos.ORIGIN));
    assertFalse(ContainerHighlights.isShownThroughBlocks(BlockPos.ORIGIN.up()));
  }

  @Test
  void aLocatedContainerStopsBeingLocatedWhenItsOutlineRunsOut() {
    ContainerHighlights.show(one(), 1, true);

    ContainerHighlights.tick();

    assertFalse(ContainerHighlights.showsThroughBlocks());
    assertFalse(ContainerHighlights.isShownThroughBlocks(BlockPos.ORIGIN));
  }

  @Test
  void aNetworkOutlineReplacesALocatedOne() {
    ContainerHighlights.show(one(), 5, true);

    ContainerHighlights.show(one(), 5, false);

    assertFalse(ContainerHighlights.showsThroughBlocks());
  }

  @AfterEach
  void stopShowing() {
    ContainerHighlights.clear();
  }

  @Test
  void whatIsShownIsWhatWasSent() {
    ContainerHighlights.show(one(), 5, false);

    assertTrue(ContainerHighlights.isShowing());
    assertEquals(one(), ContainerHighlights.showing());
  }

  @Test
  void theOutlinesRunOutAfterTheTicksTheyWereGiven() {
    ContainerHighlights.show(one(), 2, false);

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
    ContainerHighlights.show(one(), 5, false);

    ContainerHighlights.show(List.of(), 5, false);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void anEmptyAnswerShowsNothingForNoTimeAtAll() {
    ContainerHighlights.show(List.of(), 100, false);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void anAnswerOfNothingAtAllIsTreatedAsEmpty() {
    ContainerHighlights.show(null, 100, false);

    assertFalse(ContainerHighlights.isShowing());
  }

  @Test
  void clearingStopsTheOutlinesImmediately() {
    ContainerHighlights.show(one(), 100, false);

    ContainerHighlights.clear();

    assertFalse(ContainerHighlights.isShowing());
    assertTrue(ContainerHighlights.showing().isEmpty());
  }
}
