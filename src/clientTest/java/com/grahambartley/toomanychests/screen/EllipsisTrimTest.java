package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.StringVisitable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EllipsisTrimTest {
  private static final int CHARACTER_WIDTH = 6;

  private static final String LONG_NAME = "Everything And More";

  private static final EllipsisTrim.TextRoom ROOM =
      new EllipsisTrim.TextRoom() {
        @Override
        public int widthOf(StringVisitable text) {
          return plainOf(text).length() * CHARACTER_WIDTH;
        }

        @Override
        public StringVisitable trimmedTo(StringVisitable text, int width) {
          final String plain = plainOf(text);
          return StringVisitable.plain(
              plain.substring(0, Math.min(plain.length(), width / CHARACTER_WIDTH)));
        }

        @Override
        public List<Integer> lineStarts(String text, int width) {
          final int perLine = Math.max(1, width / CHARACTER_WIDTH);
          final List<Integer> starts = new ArrayList<>();
          int start = 0;
          while (start < text.length()) {
            starts.add(start);
            final int limit = start + perLine;
            if (limit >= text.length()) {
              break;
            }
            final int space = text.lastIndexOf(' ', limit);
            start = space > start ? space + 1 : limit;
          }
          return starts;
        }
      };

  private static String plainOf(StringVisitable text) {
    final StringBuilder plain = new StringBuilder();
    text.visit(
        part -> {
          plain.append(part);
          return Optional.empty();
        });
    return plain.toString();
  }

  private static String trim(String text, int characters) {
    return plainOf(
        EllipsisTrim.toWidth(StringVisitable.plain(text), characters * CHARACTER_WIDTH, ROOM));
  }

  @Test
  void aNameThatFitsIsLeftExactlyAsItWas() {
    assertEquals("Ores", trim("Ores", 4));
  }

  @Test
  void aNameThatOverflowsSaysSoWithAnEllipsis() {
    assertEquals("Everyth...", trim(LONG_NAME, 10));
  }

  @ParameterizedTest
  @ValueSource(ints = {4, 8, 12, 18})
  void aTrimmedNameNeverDrawsWiderThanTheRoomItWasGiven(int characters) {
    assertTrue(
        trim(LONG_NAME, characters).length() <= characters,
        "'" + trim(LONG_NAME, characters) + "' is wider than " + characters + " characters");
  }

  @ParameterizedTest
  @ValueSource(ints = {4, 8, 12, 18})
  void aShortenedNameAlwaysEndsInTheEllipsis(int characters) {
    assertTrue(trim(LONG_NAME, characters).endsWith(EllipsisTrim.ELLIPSIS));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -40})
  void nothingIsDrawnIntoARowWithNoRoomInIt(int characters) {
    assertEquals("", trim(LONG_NAME, characters));
  }

  @Test
  void aRowTooNarrowForEvenTheEllipsisDrawsNothingRatherThanAStub() {
    assertEquals("", trim(LONG_NAME, 3));
  }

  @Test
  void anEmptyNameIsLeftEmpty() {
    assertEquals("", trim("", 4));
  }

  @Test
  void trimmingNeedsBothSomethingToTrimAndAWayToMeasureIt() {
    assertThrows(NullPointerException.class, () -> EllipsisTrim.toWidth(null, 24, ROOM));
    assertThrows(
        NullPointerException.class,
        () -> EllipsisTrim.toWidth(StringVisitable.plain("Ores"), 24, null));
  }

  private static List<String> lines(String text, int characters, int maxLines) {
    return EllipsisTrim.toLines(text, characters * CHARACTER_WIDTH, maxLines, ROOM).stream()
        .map(EllipsisTrimTest::plainOf)
        .toList();
  }

  @Test
  void aLineThatFitsStaysOneLineExactlyAsItWas() {
    assertEquals(List.of("Ores"), lines("Ores", 10, 2));
  }

  @Test
  void aLineThatNeedsNoMoreThanTheAllowedLinesBreaksBetweenWords() {
    assertEquals(List.of("Everything", "And More"), lines(LONG_NAME, 10, 2));
  }

  @Test
  void theLastLineKeepsTheSpacesBetweenTheWordsThatDidNotFit() {
    assertEquals(
        List.of("alpha beta", "gamma del..."), lines("alpha beta gamma delta epsilon", 12, 2));
  }

  @Test
  void textBeyondTheLastAllowedLineEndsThatLineInAnEllipsis() {
    List<String> kept = lines(LONG_NAME, 5, 2);

    assertEquals(2, kept.size());
    assertEquals("Every", kept.get(0));
    assertTrue(kept.get(1).endsWith(EllipsisTrim.ELLIPSIS), kept.get(1));
  }

  @ParameterizedTest
  @CsvSource({"4, 1", "5, 2", "8, 2", "12, 3"})
  void noWrappedLineIsWiderThanTheRoomItWasGiven(int characters, int maxLines) {
    List<String> kept = lines(LONG_NAME, characters, maxLines);

    assertFalse(kept.size() > maxLines, kept.toString());
    for (String line : kept) {
      assertTrue(line.length() <= characters, "'" + line + "' is wider than " + characters);
    }
  }

  @ParameterizedTest
  @CsvSource({"0, 2", "-1, 2", "10, 0", "10, -3"})
  void noRoomOrNoLinesDrawsNothing(int characters, int maxLines) {
    assertEquals(List.of(), lines(LONG_NAME, characters, maxLines));
  }

  @Test
  void wrappingNeedsBothSomethingToWrapAndAWayToMeasureIt() {
    assertThrows(NullPointerException.class, () -> EllipsisTrim.toLines(null, 24, 2, ROOM));
    assertThrows(NullPointerException.class, () -> EllipsisTrim.toLines("Ores", 24, 2, null));
  }
}
