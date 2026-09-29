package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WrappedTooltipTest {
  private static final int CHARACTER_WIDTH = 6;

  private static final BiFunction<Text, Integer, List<OrderedText>> BY_CHARACTER =
      (line, width) -> {
        final String plain = line.getString();
        final int perLine = Math.max(1, width / CHARACTER_WIDTH);
        final List<OrderedText> parts = new ArrayList<>();
        for (int start = 0; start < plain.length(); start += perLine) {
          parts.add(
              Text.literal(plain.substring(start, Math.min(plain.length(), start + perLine)))
                  .asOrderedText());
        }
        return parts;
      };

  private static String plainOf(OrderedText text) {
    final StringBuilder plain = new StringBuilder();
    text.accept(
        (index, style, codePoint) -> {
          plain.appendCodePoint(codePoint);
          return true;
        });
    return plain.toString();
  }

  private static List<String> wrap(int width, String... lines) {
    return WrappedTooltip.wrap(
            List.of(lines).stream().map(line -> (Text) Text.literal(line)).toList(),
            width,
            BY_CHARACTER)
        .stream()
        .map(WrappedTooltipTest::plainOf)
        .toList();
  }

  @ParameterizedTest
  @CsvSource({
    "1708, 200",
    "854, 200",
    "427, 200",
    "400, 200",
    "320, 160",
    "240, 120",
    "1, 1",
    "0, 1"
  })
  void aTooltipIsNeverWiderThanHalfTheScreenNorWiderThanTheCap(int screenWidth, int expected) {
    assertEquals(expected, WrappedTooltip.widthFor(screenWidth));
  }

  @Test
  void theCapIsTwoHundredScaledPixels() {
    assertEquals(200, WrappedTooltip.MAX_WIDTH);
  }

  @Test
  void aShortLineIsLeftAsItWas() {
    assertEquals(List.of("Owner only"), wrap(120, "Owner only"));
  }

  @Test
  void aLongLineIsWrappedWithNothingLost() {
    String meaning = "The network never writes here";
    List<String> wrapped = wrap(60, meaning);

    assertEquals(List.of("The networ", "k never wr", "ites here"), wrapped);
    assertEquals(meaning, String.join("", wrapped));
  }

  @Test
  void everyLineKeepsItsPlaceInOrder() {
    assertEquals(
        List.of("Chest", "Excluded", "Never writ", "ten here"),
        wrap(60, "Chest", "Excluded", "Never written here"));
  }

  @Test
  void aBlankSeparatorLineSurvivesWrapping() {
    assertEquals(List.of("Diamond", "", "Total 3"), wrap(60, "Diamond", "", "Total 3"));
  }

  @Test
  void anEmptyTooltipWrapsToNothing() {
    assertEquals(List.of(), wrap(60));
  }
}
