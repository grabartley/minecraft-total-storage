package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.access.PlayerNameRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.glfw.GLFW;

class PlayerNameEntryTest {
  private final PlayerNameEntry entry = new PlayerNameEntry();

  private void type(String text) {
    for (char character : text.toCharArray()) {
      entry.charTyped(character);
    }
  }

  @Test
  void aNewEntryIsEmptyAndNotReady() {
    assertEquals("", entry.text());
    assertFalse(entry.isReady());
  }

  @Test
  void typingANameFillsTheEntry() {
    type("Steve_42");

    assertEquals("Steve_42", entry.text());
    assertTrue(entry.isReady());
  }

  @ParameterizedTest
  @ValueSource(chars = {' ', '-', '!', 'é'})
  void aCharacterNoPlayerNameCanHoldIsRefused(char character) {
    assertEquals(PromptKey.REFUSED, entry.charTyped(character));
    assertEquals("", entry.text());
  }

  @Test
  void typingPastTheLongestNameIsRefused() {
    type("x".repeat(PlayerNameRule.MAX_TYPED_LENGTH));

    assertEquals(PromptKey.REFUSED, entry.charTyped('y'));
    assertEquals(PlayerNameRule.MAX_TYPED_LENGTH, entry.text().length());
  }

  @Test
  void enterConfirmsANameAndIsRefusedWithNone() {
    assertEquals(PromptKey.REFUSED, entry.keyPressed(GLFW.GLFW_KEY_ENTER, false));
    type("Alex");
    assertEquals(PromptKey.CONFIRM, entry.keyPressed(GLFW.GLFW_KEY_ENTER, false));
    assertEquals(PromptKey.CONFIRM, entry.keyPressed(GLFW.GLFW_KEY_KP_ENTER, false));
  }

  @Test
  void escapeCancels() {
    assertEquals(PromptKey.CANCEL, entry.keyPressed(GLFW.GLFW_KEY_ESCAPE, false));
  }

  @Test
  void backspaceTakesOffTheLastCharacterAndIsRefusedOnceEmpty() {
    type("Al");

    assertEquals(PromptKey.HANDLED, entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false));
    assertEquals("A", entry.text());
    entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false);
    assertEquals(PromptKey.REFUSED, entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false));
  }

  @Test
  void selectingAllThenTypingReplacesTheName() {
    type("Alex");

    assertEquals(PromptKey.HANDLED, entry.keyPressed(GLFW.GLFW_KEY_A, true));
    type("Sam");

    assertEquals("Sam", entry.text());
  }

  @Test
  void clearingEmptiesTheEntry() {
    type("Alex");

    entry.clear();

    assertEquals("", entry.text());
  }
}
