package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EllipsisTrimTest {
  private static final int CHARACTER_WIDTH = 6;

  private static final ToIntFunction<String> MEASURE = text -> text.length() * CHARACTER_WIDTH;

  private static String trim(String text, int width) {
    return EllipsisTrim.toWidth(text, width, MEASURE);
  }

  private static int widthOf(int characters) {
    return characters * CHARACTER_WIDTH;
  }

  @Test
  void aNameThatFitsIsLeftExactlyAsItWas() {
    assertEquals("Ores", trim("Ores", widthOf(4)));
  }

  @Test
  void aNameThatOverflowsSaysSoWithAnEllipsis() {
    assertEquals("Ores an...", trim("Ores and Minerals", widthOf(10)));
  }

  @ParameterizedTest
  @ValueSource(ints = {8, 10, 12, 16})
  void aTrimmedNameNeverDrawsWiderThanTheRoomItWasGiven(int characters) {
    String trimmed = trim("Weapons and Armour", widthOf(characters));

    assertTrue(
        MEASURE.applyAsInt(trimmed) <= widthOf(characters),
        "'" + trimmed + "' is wider than " + characters + " characters");
  }

  @Test
  void aTrimmedNameAlwaysEndsInTheEllipsisRatherThanMidWord() {
    assertTrue(trim("Weapons and Armour", widthOf(9)).endsWith(EllipsisTrim.ELLIPSIS));
  }

  @Test
  void theSpaceBeforeAnEllipsisIsDroppedSoTheGapDoesNotRead() {
    assertEquals("Ores...", trim("Ores and Minerals", widthOf(8)));
  }

  @ParameterizedTest
  @CsvSource({"0", "-1", "-40"})
  void nothingIsDrawnIntoARowWithNoRoomInIt(int width) {
    assertEquals("", trim("Ores and Minerals", width));
  }

  @Test
  void aRowTooNarrowForEvenTheEllipsisDrawsNothingRatherThanAStub() {
    assertEquals("", trim("Ores and Minerals", widthOf(3)));
  }

  @Test
  void anEmptyNameIsLeftEmpty() {
    assertEquals("", trim("", widthOf(4)));
  }

  @Test
  void trimmingNeedsBothSomethingToTrimAndAWayToMeasureIt() {
    assertThrows(NullPointerException.class, () -> EllipsisTrim.toWidth(null, widthOf(4), MEASURE));
    assertThrows(NullPointerException.class, () -> EllipsisTrim.toWidth("Ores", widthOf(4), null));
  }
}
