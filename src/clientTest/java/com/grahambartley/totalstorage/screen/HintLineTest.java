package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

class HintLineTest {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".terminal.";

  private static TranslatableTextContent translationOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  @Test
  void aKeyIsLabelledFromTheTerminalsKeyTranslations() {
    final HintLine.Key key = assertInstanceOf(HintLine.Key.class, HintLine.key("shift"));

    assertEquals(PREFIX + "key.shift", translationOf(key.label()).getKey());
  }

  @Test
  void wordsByKeyAreTranslatedWithTheirArguments() {
    final HintLine.Words words =
        assertInstanceOf(HintLine.Words.class, HintLine.words("hint.take", "64"));

    assertEquals(PREFIX + "hint.take", translationOf(words.text()).getKey());
    assertArrayEquals(new Object[] {"64"}, translationOf(words.text()).getArgs());
  }

  @Test
  void wordsGivenAsTextAreKeptAsTheyAre() {
    final Text text = Text.literal("drop one");

    assertSame(text, ((HintLine.Words) HintLine.words(text)).text());
  }

  @Test
  void aBadgeIsTheLockBadge() {
    assertInstanceOf(HintLine.LockBadge.class, HintLine.badge());
  }

  @Test
  void aLineKeepsItsPartsInOrder() {
    final HintLine.Part key = HintLine.key("shift");
    final HintLine.Part words = HintLine.words(Text.literal("move"));

    assertEquals(List.of(key, words), HintLine.of(key, words).parts());
  }

  @Test
  void aLineIsNotChangedByLaterEditsToTheListItWasBuiltFrom() {
    final List<HintLine.Part> parts = new ArrayList<>(List.of(HintLine.badge()));
    final HintLine line = new HintLine(parts);

    parts.add(HintLine.key("shift"));

    assertEquals(1, line.parts().size());
    assertThrows(UnsupportedOperationException.class, () -> line.parts().add(HintLine.badge()));
  }

  @Test
  void aKeyOrWordsWithoutTextAreRefused() {
    assertThrows(NullPointerException.class, () -> new HintLine.Key(null));
    assertThrows(NullPointerException.class, () -> new HintLine.Words(null));
  }
}
