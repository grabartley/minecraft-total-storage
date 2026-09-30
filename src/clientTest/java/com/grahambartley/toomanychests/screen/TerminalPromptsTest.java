package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.tag.TagNameOutcome;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
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
    assertFalse(prompts.keyPressed(keyCode, 0, NetworkTagView.pending()));
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

    assertTrue(prompts.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, NetworkTagView.pending()));
    assertFalse(prompts.isAnyOpen());
  }

  @Test
  void aClickAnywhereDismissesTheNamePrompt() {
    prompts.naming().openForNewTag();

    assertTrue(
        prompts.mouseClicked(
            NetworkSettingsView.pending(), null, false, 0, 0, GridClick.LEFT_BUTTON));
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

  private static NetworkTagView tagsOf(NetworkTagEntry... tags) {
    return new NetworkTagView(true, List.of(tags), Optional.empty(), Set.of());
  }

  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, id, true, 0, 0);
  }

  private static NetworkTagEntry custom(String id) {
    return new NetworkTagEntry(id, id, false, 0, 0);
  }

  private void type(String text) {
    for (char typed : text.toCharArray()) {
      prompts.charTyped(typed);
    }
  }

  @Test
  void aNameThatBelongsToAPresetKeepsThePromptOpenWithTheTextAndSaysWhy() {
    prompts.naming().openForNewTag();
    type("Stone");

    assertTrue(prompts.keyPressed(GLFW.GLFW_KEY_ENTER, 0, tagsOf(preset("stone"))));

    assertTrue(prompts.naming().isOpen());
    assertEquals("Stone", prompts.naming().name());
    assertEquals(TagNameOutcome.PRESET_COLLISION, prompts.naming().refusal());
  }

  @Test
  void aNameAnotherCustomTagHasKeepsThePromptOpenWithTheTextAndSaysWhy() {
    prompts.naming().openForNewTag();
    type("Castle");

    prompts.keyPressed(GLFW.GLFW_KEY_ENTER, 0, tagsOf(preset("stone"), custom("castle")));

    assertTrue(prompts.naming().isOpen());
    assertEquals("Castle", prompts.naming().name());
    assertEquals(TagNameOutcome.DUPLICATE, prompts.naming().refusal());
  }

  @Test
  void renamingOntoAnotherTagsNameIsRefusedInThePrompt() {
    prompts.naming().openForRename("castle", "Castle");
    for (int deleted = 0; deleted < "Castle".length(); deleted++) {
      prompts.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, NetworkTagView.pending());
    }
    type("Keep");

    prompts.keyPressed(GLFW.GLFW_KEY_ENTER, 0, tagsOf(custom("castle"), custom("keep")));

    assertTrue(prompts.naming().isOpen());
    assertEquals(TagNameOutcome.DUPLICATE, prompts.naming().refusal());
  }

  @Test
  void theFilterPromptsScrollbarCanBeGrabbedDraggedAndLetGo() {
    NetworkTagView thirteen =
        tagsOf(
            IntStream.range(0, 13)
                .mapToObj(i -> custom("tag" + i))
                .toArray(NetworkTagEntry[]::new));
    prompts.filtering().open(new BlockPos(1, 2, 3));
    prompts.fit(NetworkSettingsView.pending(), thirteen);

    assertTrue(
        prompts.mouseClicked(
            NetworkSettingsView.pending(),
            thirteen,
            false,
            TagPromptLayout.scrollbarX(),
            TagPromptLayout.rowsTop(),
            GridClick.LEFT_BUTTON));
    assertTrue(prompts.isDraggingScrollbar());

    prompts.dragScrollbarTo(TagPromptLayout.rowsTop() + TagPromptLayout.TRACK_HEIGHT);
    assertEquals(
        "tag9",
        prompts
            .filtering()
            .tagAt(thirteen, TagPromptLayout.textLeft(), TagPromptLayout.rowY(0))
            .id());

    prompts.releaseScrollbar();
    assertFalse(prompts.isDraggingScrollbar());
    assertTrue(prompts.filtering().isOpen());
  }

  @Test
  void withNoListPromptUpThereIsNoScrollbarToDrag() {
    prompts.naming().openForNewTag();

    assertFalse(prompts.isDraggingScrollbar());
  }

  @Test
  void editingTheTrustListTakesTheKeyboardAndTheWheel() {
    prompts.trusting().open();

    assertTrue(prompts.isAnyOpen());
    assertTrue(prompts.ownsTheKeyboard());
    assertTrue(prompts.scrolled(1.0));
  }

  @Test
  void typingWhileEditingTheTrustListReachesTheName() {
    prompts.trusting().open();

    assertTrue(prompts.charTyped('A'));
    assertEquals("A", prompts.trusting().name());
  }

  @Test
  void escapeClosesTheTrustPrompt() {
    prompts.trusting().open();

    assertTrue(prompts.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, NetworkTagView.pending()));
    assertFalse(prompts.isAnyOpen());
  }

  @Test
  void aClickOutsideTheTrustPromptDismissesIt() {
    prompts.trusting().open();

    assertTrue(
        prompts.mouseClicked(
            NetworkSettingsView.pending(), NetworkTagView.pending(), false, 0, 0, 0));
    assertFalse(prompts.isAnyOpen());
  }

  @Test
  void theTrustPromptClosesOnceTheViewerMayNoLongerAdministerTheTerminal() {
    prompts.trusting().open();

    prompts.fit(
        NetworkSettingsView.answered(
            4,
            4,
            16,
            TerminalAccessMode.OPEN,
            false,
            true,
            List.of(),
            NetworkCapacity.EMPTY,
            0,
            0,
            List.of()),
        NetworkTagView.pending());

    assertFalse(prompts.trusting().isOpen());
  }
}
