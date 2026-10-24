package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
  void nothingTypedMeansEverythingTheNetworkHolds() {
    AmountEntry entry = new AmountEntry(500);

    assertEquals("", entry.text());
    assertTrue(entry.takesEverything());
    assertEquals(500, entry.value());
    assertTrue(entry.isReady());
  }

  @Test
  void nothingTypedOnAnEmptyNetworkIsStillNotReady() {
    AmountEntry entry = new AmountEntry(0);

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

  @ParameterizedTest
  @CsvSource({"69, 100, 100, 69", "69, 70, 70, 69", "69, 69, 69, 69", "69, 7, 7, 7"})
  void whatWasAskedForIsKeptAndTheWithdrawalIsClampedOnConfirm(
      int ceiling, String digits, int requested, int taken) {
    AmountEntry entry = typing(ceiling, digits);

    assertEquals(digits, entry.text());
    assertEquals(requested, entry.requested());
    assertEquals(taken, entry.value());
    assertEquals(requested > ceiling, entry.asksForMoreThanThereIs());
  }

  @Test
  void aNumberWiderThanTheEntryAllowsIsRefusedRatherThanOverflowing() {
    AmountEntry entry = typing(64, "9".repeat(AmountEntry.MAX_DIGITS));

    assertFalse(entry.append('9'));
    assertEquals(AmountEntry.MAX_DIGITS, entry.text().length());
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
  void selectAllReplacesTheWholeNumberWithTheNextDigit() {
    AmountEntry entry = typing(500, "128");

    assertTrue(entry.selectAll());
    assertTrue(entry.append('4'));
    assertEquals("4", entry.text());
  }

  @Test
  void selectAllThenBackspaceClearsTheWholeNumber() {
    AmountEntry entry = typing(500, "128");

    entry.selectAll();

    assertTrue(entry.backspace());
    assertEquals("", entry.text());
  }

  @Test
  void selectAllThenAZeroIsRefusedRatherThanQuietlyMeaningEverything() {
    AmountEntry entry = typing(500, "128");

    entry.selectAll();

    assertFalse(entry.append('0'));
    assertEquals("128", entry.text());
    assertFalse(entry.takesEverything());
  }

  @Test
  void selectAllOnAnEmptyNumberHasNothingToSelect() {
    assertFalse(new AmountEntry(500).selectAll());
  }

  @Test
  void selectAllOnlyAnswersToTheControlKey() {
    AmountEntry withControl = typing(500, "128");
    AmountEntry withoutControl = typing(500, "128");

    assertEquals(PromptKey.HANDLED, withControl.keyPressed(GLFW.GLFW_KEY_A, true));
    assertEquals(PromptKey.HANDLED, withoutControl.keyPressed(GLFW.GLFW_KEY_A, false));

    withControl.append('4');
    withoutControl.append('4');

    assertEquals("4", withControl.text());
    assertEquals("1284", withoutControl.text());
  }

  @Test
  void escapeCancelsWhateverWasTyped() {
    assertEquals(PromptKey.CANCEL, typing(500, "12").keyPressed(GLFW.GLFW_KEY_ESCAPE, false));
    assertEquals(PromptKey.CANCEL, new AmountEntry(500).keyPressed(GLFW.GLFW_KEY_ESCAPE, false));
  }

  @ParameterizedTest
  @ValueSource(ints = {GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER})
  void confirmingOnAnEmptyFieldTakesEverything(int enterKey) {
    assertEquals(PromptKey.CONFIRM, new AmountEntry(500).keyPressed(enterKey, false));
    assertEquals(PromptKey.REFUSED, new AmountEntry(0).keyPressed(enterKey, false));
    assertEquals(PromptKey.CONFIRM, typing(500, "12").keyPressed(enterKey, false));
  }

  @ParameterizedTest
  @ValueSource(ints = {GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_F3})
  void everyKeyThatIsNotAnEndingIsKeptRatherThanPassedOn(int keyCode) {
    assertEquals(PromptKey.HANDLED, typing(500, "12").keyPressed(keyCode, false));
    assertEquals(PromptKey.HANDLED, new AmountEntry(500).keyPressed(keyCode, false));
  }

  @Test
  void aRefusedKeystrokeSaysSoRatherThanPassingForAnEdit() {
    assertEquals(PromptKey.REFUSED, new AmountEntry(500).charTyped('x'));
    assertEquals(PromptKey.HANDLED, new AmountEntry(500).charTyped('4'));
    assertEquals(
        PromptKey.REFUSED, new AmountEntry(500).keyPressed(GLFW.GLFW_KEY_BACKSPACE, false));
  }

  @Test
  void aSwallowedKeyStillReachesTheNumber() {
    AmountEntry entry = typing(500, "12");

    entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false);

    assertEquals("1", entry.text());
  }

  @Test
  void aDigitThatLeavesTheNumberUnchangedIsNotAnEdit() {
    assertFalse(new AmountEntry(500).append('0'));
    assertTrue(new AmountEntry(500).append('7'));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Integer.MIN_VALUE})
  void aCeilingBelowNothingIsTreatedAsNothing(int ceiling) {
    assertEquals(0, new AmountEntry(ceiling).ceiling());
  }
}
