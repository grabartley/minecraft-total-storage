package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.grahambartley.totalstorage.screen.HintLine;
import com.grahambartley.totalstorage.screen.HintTooltipData;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LockTooltipLinesTest {
  private static final String PREFIX = "screen.total-storage.lock.";

  private static final Text NAME = Text.literal("Diamond Sword");

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  @ParameterizedTest(name = "alt={0} locked={1}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  void theLockLinesOnlyApplyToALockedSlotWhileAltIsHeld(
      boolean altDown, boolean locked, boolean applies) {
    assertEquals(applies, LockTooltipLines.applies(altDown, locked));
  }

  @ParameterizedTest
  @CsvSource({"false, alt", "true, option"})
  void theHintsShowTheBadgeMeaningLockedThenTheModifierToUnlock(boolean onMac, String key) {
    final List<HintLine> hints = LockTooltipLines.hints(onMac);

    assertEquals(2, hints.size());
    assertEquals(new HintLine.LockBadge(), hints.get(0).parts().get(0));
    assertEquals(PREFIX + "locked", keyOf(((HintLine.Words) hints.get(0).parts().get(1)).text()));
    assertEquals(
        "screen.total-storage.terminal.key." + key,
        keyOf(((HintLine.Key) hints.get(1).parts().get(0)).label()));
    assertEquals(PREFIX + "unlock", keyOf(((HintLine.Words) hints.get(1).parts().get(1)).text()));
  }

  @Test
  void anItemWithoutItsOwnTooltipDataGetsTheLockHints() {
    final Optional<TooltipData> data = LockTooltipLines.withHints(Optional.empty(), false);

    assertEquals(Optional.of(new HintTooltipData(LockTooltipLines.hints(false))), data);
  }

  @Test
  void anItemWithItsOwnTooltipDataKeepsIt() {
    final Optional<TooltipData> own = Optional.of(mock(TooltipData.class));

    assertSame(own, LockTooltipLines.withHints(own, true));
  }

  @Test
  void anItemWhoseHintsAreDrawnAsDataNeedsNoExtraText() {
    final List<Text> lines = List.of(NAME);

    assertSame(lines, LockTooltipLines.withText(lines, false));
  }

  @Test
  void anItemWithItsOwnDataGetsTheLockExplainedInPlainText() {
    final List<Text> lines = List.of(NAME);

    final List<Text> extended = LockTooltipLines.withText(lines, true);

    assertEquals(List.of(NAME), lines);
    assertEquals(3, extended.size());
    assertSame(NAME, extended.get(0));
    assertEquals(PREFIX + "locked", keyOf(extended.get(1)));
    assertEquals(Formatting.GRAY.getColorValue(), extended.get(1).getStyle().getColor().getRgb());
    assertEquals(PREFIX + "unlock.plain", keyOf(extended.get(2)));
    assertEquals(
        Formatting.DARK_GRAY.getColorValue(), extended.get(2).getStyle().getColor().getRgb());
  }
}
