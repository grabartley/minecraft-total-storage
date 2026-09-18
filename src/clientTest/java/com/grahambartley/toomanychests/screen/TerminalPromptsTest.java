package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.glfw.GLFW;

class TerminalPromptsTest {
  private static final int SYNC_ID = 7;

  private final TerminalPrompts prompts = new TerminalPrompts(SYNC_ID);

  @Test
  void nothingIsOpenOnAFreshScreen() {
    assertFalse(prompts.isAnyOpen());
    assertFalse(prompts.ownsTheKeyboard());
    assertFalse(prompts.isTagging());
  }

  @ParameterizedTest
  @ValueSource(ints = {GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_A})
  void withNoPromptUpEveryKeyIsLeftToTheScreen(int keyCode) {
    assertFalse(prompts.keyPressed(keyCode, 0));
  }

  @Test
  void withNoPromptUpTypingIsLeftToTheSearchBox() {
    assertFalse(prompts.charTyped('r'));
  }

  @Test
  void withNoPromptUpScrollingIsLeftToTheGrid() {
    assertFalse(prompts.scrolled(1.0));
    assertFalse(prompts.scrolled(0.0));
  }

  @Test
  void namingATagTakesTheKeyboardAndTheWheel() {
    prompts.naming().openForNewTag();

    assertTrue(prompts.isAnyOpen());
    assertTrue(prompts.ownsTheKeyboard());
    assertTrue(prompts.scrolled(1.0));
    assertFalse(prompts.isTagging());
  }

  @Test
  void typingWhileNamingReachesTheNameRatherThanTheScreen() {
    prompts.naming().openForNewTag();

    assertTrue(prompts.charTyped('c'));
    assertEquals("c", prompts.naming().name());
  }

  @Test
  void escapeClosesTheNamePromptWithoutClosingTheScreen() {
    prompts.naming().openForNewTag();

    assertTrue(prompts.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0));
    assertFalse(prompts.isAnyOpen());
  }

  @Test
  void aClickAnywhereDismissesTheNamePrompt() {
    prompts.naming().openForNewTag();

    assertTrue(prompts.mouseClicked(null, false, 0, 0, GridClick.LEFT_BUTTON));
    assertFalse(prompts.isAnyOpen());
  }

  @Test
  void renamingStartsFromTheNameTheTagAlreadyHas() {
    prompts.naming().openForRename("castle", "Castle");

    assertTrue(prompts.naming().isRenaming());
    assertEquals("castle", prompts.naming().renaming());
    assertEquals("Castle", prompts.naming().name());
  }

  @Test
  void aNewTagIsNotARename() {
    prompts.naming().openForNewTag();

    assertFalse(prompts.naming().isRenaming());
    assertEquals("", prompts.naming().renaming());
  }
}
