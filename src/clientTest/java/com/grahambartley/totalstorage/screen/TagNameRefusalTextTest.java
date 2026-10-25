package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.totalstorage.tag.TagNameOutcome;
import com.grahambartley.totalstorage.tag.TagNameRule;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TagNameRefusalTextTest {
  private static final String PREFIX = "screen.total-storage.terminal.tags.name.refused.";

  private static TranslatableTextContent contentOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "BLANK, blank",
    "ILLEGAL_CHARACTER, illegal",
    "DUPLICATE, duplicate",
    "PRESET_COLLISION, preset"
  })
  void eachRefusalNamesItsReason(TagNameOutcome outcome, String reason) {
    final TranslatableTextContent content = contentOf(TagNameRefusalText.of(outcome));

    assertEquals(PREFIX + reason, content.getKey());
    assertEquals(0, content.getArgs().length);
  }

  @Test
  void anAcceptedNameHasNoRefusalToShow() {
    assertThrows(IllegalArgumentException.class, () -> TagNameRefusalText.of(TagNameOutcome.OK));
  }

  @Test
  void aNameThatIsTooLongSaysHowLongANameMayBe() {
    final TranslatableTextContent content =
        contentOf(TagNameRefusalText.of(TagNameOutcome.TOO_LONG));

    assertEquals(PREFIX + "too_long", content.getKey());
    assertArrayEquals(new Object[] {TagNameRule.MAX_LENGTH}, content.getArgs());
  }
}
