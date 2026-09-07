package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.glfw.GLFW;

class AmountEntryTest {

  private static AmountEntry typing(int ceiling, String digits) {
    AmountEntry entry = new AmountEntry(ceiling);
    for (final char digit : digits.toCharArray()) {
      entry.append(digit);
    }
    return entry;
  }

  @Test
  void nothingTypedIsNotANumberYet() {
    AmountEntry entry = new AmountEntry(500);

    assertEquals("", entry.text());
    assertEquals(0, entry.value());
    assertFalse(entry.isReady());
  }

  @Test
  void digitsAccumulateIntoTheNumberTheyRead() {
    assertEquals(128, typing(500, "128").value());
    assertEquals("128", typing(500, "128").text());
  }

  @ParameterizedTest
  @ValueSource(chars = {'a', '-', '.', ' ', '+'})
  void anythingThatIsNotADigitIsRefused(char typed) {
    AmountEntry entry = new AmountEntry(500);

    assertFalse(entry.append(typed));
    assertEquals("", entry.text());
  }

  /** A field that accepts a number it will then quietly shrink has lied about what will happen. */
  @Test
  void aDigitThatWouldPushTheNumberPastWhatExistsIsRefused() {
    AmountEntry entry = typing(64, "6");

    assertFalse(entry.append('5'));
    assertEquals(6, entry.value());
    assertTrue(entry.append('4'));
    assertEquals(64, entry.value());
  }

  @Test
  void aNetworkHoldingNothingTakesNoDigitsAtAll() {
    AmountEntry entry = new AmountEntry(0);

    assertFalse(entry.append('1'));
    assertEquals(0, entry.value());
  }

  @Test
  void leadingZerosAreDroppedRatherThanStored() {
    AmountEntry entry = typing(500, "00");

    assertEquals("", entry.text());
    assertTrue(entry.append('7'));
    assertEquals(7, entry.value());
  }

  @Test
  void backspaceTakesBackTheLastDigitAndNothingMore() {
    AmountEntry entry = typing(500, "128");

    assertTrue(entry.backspace());
    assertEquals(12, entry.value());
  }

  @Test
  void backspaceOnAnEmptyNumberChangesNothing() {
    assertFalse(new AmountEntry(500).backspace());
  }

  @Test
  void escapeCancelsWhateverWasTyped() {
    assertEquals(PromptKey.CANCEL, typing(500, "12").keyPressed(GLFW.GLFW_KEY_ESCAPE));
    assertEquals(PromptKey.CANCEL, new AmountEntry(500).keyPressed(GLFW.GLFW_KEY_ESCAPE));
  }

  @ParameterizedTest
  @ValueSource(ints = {GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER})
  void confirmingNeedsANumberToConfirm(int enterKey) {
    assertEquals(PromptKey.IGNORED, new AmountEntry(500).keyPressed(enterKey));
    assertEquals(PromptKey.CONFIRM, typing(500, "12").keyPressed(enterKey));
  }

  @Test
  void aKeyThatChangesTheNumberSaysSoSoThePromptRedraws() {
    assertEquals(PromptKey.EDITED, new AmountEntry(500).charTyped('4'));
    assertEquals(PromptKey.EDITED, typing(500, "4").keyPressed(GLFW.GLFW_KEY_BACKSPACE));
    assertEquals(PromptKey.IGNORED, new AmountEntry(500).keyPressed(GLFW.GLFW_KEY_BACKSPACE));
    assertEquals(PromptKey.IGNORED, new AmountEntry(500).charTyped('x'));
  }

  @ParameterizedTest
  @ValueSource(ints = {GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_F3})
  void aKeyWithNoMeaningHereIsIgnoredRatherThanPassedOn(int keyCode) {
    assertEquals(PromptKey.IGNORED, typing(500, "12").keyPressed(keyCode));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void aCeilingBelowNothingIsTreatedAsNothing(int ceiling) {
    assertEquals(0, new AmountEntry(ceiling).ceiling());
  }
}
