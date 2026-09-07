package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalScreenTextTest {
  private static String keyOf(Text line) {
    return ((TranslatableTextContent) line.getContent()).getKey();
  }

  @DisplayName("every line the screen shows lives under the one prefix")
  @ParameterizedTest(name = "{0} becomes {1}")
  @CsvSource({
    "total, screen.too-many-chests.terminal.total",
    "containers, screen.too-many-chests.terminal.containers",
    "empty.network, screen.too-many-chests.terminal.empty.network",
    "offline, screen.too-many-chests.terminal.offline",
  })
  void buildsTheKeyFromTheSharedPrefix(String suffix, String expected) {
    assertEquals(expected, keyOf(TerminalScreenText.of(suffix)));
  }

  @Test
  void aLineCarriesWhateverItWasGivenToFillIn() {
    Text line = TerminalScreenText.of("total", "1,024");

    assertTrue(
        Arrays.asList(((TranslatableTextContent) line.getContent()).getArgs()).contains("1,024"));
  }
}
