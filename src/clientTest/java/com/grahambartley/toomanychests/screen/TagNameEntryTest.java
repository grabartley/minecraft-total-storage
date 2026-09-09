package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.tag.TagNameRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.glfw.GLFW;

class TagNameEntryTest {
  @Test
  void anEntryStartsFromTheNameItWasGiven() {
    assertEquals("Castle", new TagNameEntry("Castle").text());
    assertEquals("Castle", new TagNameEntry("  Castle  ").text());
    assertEquals("", new TagNameEntry(null).text());
  }

  @Test
  void aStartingNameLongerThanTheLimitIsCutToFit() {
    final TagNameEntry entry = new TagNameEntry("a".repeat(TagNameRule.MAX_LENGTH + 10));
    assertEquals(TagNameRule.MAX_LENGTH, entry.text().length());
  }

  @ParameterizedTest
  @ValueSource(chars = {'a', 'Z', '7', '-', '_'})
  void aCharacterATagNameMayCarryIsTaken(char typed) {
    final TagNameEntry entry = new TagNameEntry("");
    assertTrue(entry.append(typed));
    assertEquals(String.valueOf(typed), entry.text());
  }

  @ParameterizedTest
  @ValueSource(chars = {'#', '@', ':', '/', '*', '.'})
  void aCharacterTheQueryLanguageUsesIsRefusedAsItIsTyped(char typed) {
    final TagNameEntry entry = new TagNameEntry("");
    assertFalse(entry.append(typed));
    assertEquals("", entry.text());
  }

  @Test
  void aNameNeverStartsWithASpaceOrDoublesOne() {
    final TagNameEntry entry = new TagNameEntry("");
    assertFalse(entry.append(' '));
    entry.append('a');
    assertTrue(entry.append(' '));
    assertFalse(entry.append(' '));
    assertEquals("a ", entry.text());
  }

  @Test
  void typingStopsAtTheLimitRatherThanTruncatingLater() {
    final TagNameEntry entry = new TagNameEntry("a".repeat(TagNameRule.MAX_LENGTH));
    assertFalse(entry.append('b'));
    assertEquals(TagNameRule.MAX_LENGTH, entry.text().length());
  }

  @Test
  void backspaceReportsWhetherItHadAnythingToRemove() {
    final TagNameEntry entry = new TagNameEntry("ab");
    assertTrue(entry.backspace());
    assertEquals("a", entry.text());
    assertTrue(entry.backspace());
    assertFalse(entry.backspace());
  }

  @Test
  void anEmptyNameIsNotReadyToSave() {
    assertFalse(new TagNameEntry("").isReady());
    assertTrue(new TagNameEntry("castle").isReady());
  }

  @Test
  void escapeCancelsAndEnterConfirmsOnlyWhenThereIsANameToSave() {
    assertEquals(PromptKey.CANCEL, new TagNameEntry("castle").keyPressed(GLFW.GLFW_KEY_ESCAPE));
    assertEquals(PromptKey.CONFIRM, new TagNameEntry("castle").keyPressed(GLFW.GLFW_KEY_ENTER));
    assertEquals(PromptKey.HANDLED, new TagNameEntry("").keyPressed(GLFW.GLFW_KEY_ENTER));
  }

  @Test
  void backspaceIsHandledWithoutClosingThePrompt() {
    final TagNameEntry entry = new TagNameEntry("ab");
    assertEquals(PromptKey.HANDLED, entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE));
    assertEquals("a", entry.text());
  }

  @Test
  void everyTypedCharacterIsSwallowedByThePrompt() {
    final TagNameEntry entry = new TagNameEntry("");
    assertEquals(PromptKey.HANDLED, entry.charTyped('c'));
    assertEquals(PromptKey.HANDLED, entry.charTyped('#'));
    assertEquals("c", entry.text());
  }
}
