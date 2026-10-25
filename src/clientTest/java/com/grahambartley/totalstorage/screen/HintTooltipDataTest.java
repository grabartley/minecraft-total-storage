package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HintTooltipDataTest {
  @Test
  void theHintsAreKeptAsTheyWereWhenTheTooltipWasMade() {
    final List<HintLine> given = new ArrayList<>(List.of(HintLine.of(HintLine.key("esc"))));
    final HintTooltipData data = new HintTooltipData(given);

    given.add(HintLine.of(HintLine.key("enter")));

    assertEquals(1, data.lines().size());
  }

  @Test
  void theHintsCannotBeChangedAfterwards() {
    final HintTooltipData data = new HintTooltipData(List.of(HintLine.of(HintLine.key("esc"))));

    assertThrows(
        UnsupportedOperationException.class,
        () -> data.lines().add(HintLine.of(HintLine.key("enter"))));
  }
}
