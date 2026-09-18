package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.ContainerNameRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.glfw.GLFW;

class ContainerNameEntryTest {
  private static ContainerNameEntry typing(String text) {
    ContainerNameEntry entry = new ContainerNameEntry("");
    for (char character : text.toCharArray()) {
      entry.append(character);
    }
    return entry;
  }

  @Test
  void whatIsTypedIsWhatTheFieldShows() {
    assertEquals("Potions", typing("Potions").text());
  }

  @Test
  void anEntryStartsFromTheNameTheContainerAlreadyHas() {
    assertEquals("Potions", new ContainerNameEntry("  Potions  ").text());
  }

  @Test
  void aSpaceBetweenWordsIsKept() {
    assertEquals("Ores and ingots", typing("Ores and ingots").text());
  }

  @ParameterizedTest
  @ValueSource(chars = {' '})
  void aLeadingSpaceIsRefusedRatherThanShown(char space) {
    ContainerNameEntry entry = new ContainerNameEntry("");

    assertFalse(entry.append(space));
    assertEquals("", entry.text());
  }

  @Test
  void aSecondSpaceInARowIsRefused() {
    ContainerNameEntry entry = typing("Ores ");

    assertFalse(entry.append(' '));
    assertEquals("Ores ", entry.text());
  }

  @Test
  void aControlCharacterNeverReachesTheField() {
    ContainerNameEntry entry = new ContainerNameEntry("");

    assertFalse(entry.append('\n'));
    assertEquals("", entry.text());
  }

  @Test
  void theFieldStopsAtTheLengthBound() {
    ContainerNameEntry entry = typing("x".repeat(ContainerNameRule.MAX_LENGTH));

    assertFalse(entry.append('x'));
    assertEquals(ContainerNameRule.MAX_LENGTH, entry.text().length());
  }

  @Test
  void aTrailingSpaceIsTrimmedOffTheNameThatIsSent() {
    assertEquals("Ores", typing("Ores ").name());
  }

  @Test
  void anEmptyFieldAsksForTheNameToBeTakenOff() {
    assertFalse(new ContainerNameEntry("Potions").clearsTheName());
    assertTrue(new ContainerNameEntry("").clearsTheName());
  }

  @Test
  void enterConfirmsEvenWithNothingTypedBecauseThatClearsTheName() {
    assertEquals(
        PromptKey.CONFIRM, new ContainerNameEntry("").keyPressed(GLFW.GLFW_KEY_ENTER, false));
  }

  @Test
  void escapeCancels() {
    assertEquals(
        PromptKey.CANCEL,
        new ContainerNameEntry("Potions").keyPressed(GLFW.GLFW_KEY_ESCAPE, false));
  }

  @Test
  void backspaceOnAnEmptyFieldIsRefusedRatherThanSilentlyIgnored() {
    assertEquals(
        PromptKey.REFUSED, new ContainerNameEntry("").keyPressed(GLFW.GLFW_KEY_BACKSPACE, false));
  }

  @Test
  void selectingAllThenTypingReplacesTheWholeName() {
    ContainerNameEntry entry = new ContainerNameEntry("Potions");

    assertEquals(PromptKey.HANDLED, entry.keyPressed(GLFW.GLFW_KEY_A, true));
    assertTrue(entry.append('O'));
    assertEquals("O", entry.text());
  }

  @Test
  void selectingAllOnAnEmptyFieldIsRefused() {
    assertEquals(PromptKey.REFUSED, new ContainerNameEntry("").keyPressed(GLFW.GLFW_KEY_A, true));
  }
}
