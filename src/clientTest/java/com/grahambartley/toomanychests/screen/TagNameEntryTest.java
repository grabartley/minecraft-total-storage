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
    assertEquals(
        PromptKey.CANCEL, new TagNameEntry("castle").keyPressed(GLFW.GLFW_KEY_ESCAPE, false));
    assertEquals(
        PromptKey.CONFIRM, new TagNameEntry("castle").keyPressed(GLFW.GLFW_KEY_ENTER, false));
    assertEquals(PromptKey.REFUSED, new TagNameEntry("").keyPressed(GLFW.GLFW_KEY_ENTER, false));
  }

  @Test
  void backspaceIsHandledWithoutClosingThePrompt() {
    final TagNameEntry entry = new TagNameEntry("ab");
    assertEquals(PromptKey.HANDLED, entry.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false));
    assertEquals("a", entry.text());
  }

  @Test
  void aRefusedCharacterSaysSoRatherThanPassingForAnEdit() {
    final TagNameEntry entry = new TagNameEntry("");
    assertEquals(PromptKey.HANDLED, entry.charTyped('c'));
    assertEquals(PromptKey.REFUSED, entry.charTyped('#'));
    assertEquals("c", entry.text());
  }

  @Test
  void selectAllReplacesTheWholeNameWithTheNextCharacter() {
    final TagNameEntry entry = new TagNameEntry("Base Kit");

    assertTrue(entry.selectAll());
    entry.append('K');
    entry.append('i');
    entry.append('t');

    assertEquals("Kit", entry.text());
  }

  @Test
  void selectAllThenBackspaceClearsTheWholeName() {
    final TagNameEntry entry = new TagNameEntry("Base Kit");

    entry.selectAll();

    assertTrue(entry.backspace());
    assertEquals("", entry.text());
  }

  @Test
  void selectAllOnAnEmptyNameHasNothingToSelect() {
    assertFalse(new TagNameEntry("").selectAll());
  }

  @Test
  void selectAllOnlyAnswersToTheControlKey() {
    final TagNameEntry withControl = new TagNameEntry("Base Kit");
    final TagNameEntry withoutControl = new TagNameEntry("Base Kit");

    assertEquals(PromptKey.HANDLED, withControl.keyPressed(GLFW.GLFW_KEY_A, true));
    assertEquals(PromptKey.HANDLED, withoutControl.keyPressed(GLFW.GLFW_KEY_A, false));

    withControl.append('x');
    withoutControl.append('x');

    assertEquals("x", withControl.text());
    assertEquals("Base Kitx", withoutControl.text());
  }

  @Test
  void aReplacedNameStillRefusesToOpenWithASpace() {
    final TagNameEntry entry = new TagNameEntry("Base Kit");

    entry.selectAll();

    assertFalse(entry.append(' '));
  }
}
