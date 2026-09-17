package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.text.StringVisitable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
