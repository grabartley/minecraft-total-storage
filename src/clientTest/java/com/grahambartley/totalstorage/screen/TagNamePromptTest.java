package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.tag.TagNameOutcome;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class TagNamePromptTest {
  private static final Set<String> KNOWN = Set.of("stone", "wood", "castle");

  private final TagNamePrompt prompt = new TagNamePrompt();

  private void type(String text) {
    for (char typed : text.toCharArray()) {
      prompt.charTyped(typed);
    }
  }

  @Test
  void aFreshPromptHasRefusedNothing() {
    prompt.openForNewTag();

    assertEquals(TagNameOutcome.OK, prompt.refusal());
  }

  @Test
  void aFreeNameIsAccepted() {
    prompt.openForNewTag();
    type("Base Kit");

    assertTrue(prompt.confirm(KNOWN));
    assertEquals(TagNameOutcome.OK, prompt.refusal());
  }

  @Test
  void aTakenNameIsRefusedAndTheTextIsKept() {
    prompt.openForNewTag();
    type("Castle");

    assertFalse(prompt.confirm(KNOWN));
    assertEquals(TagNameOutcome.DUPLICATE, prompt.refusal());
    assertEquals("Castle", prompt.name());
    assertTrue(prompt.isOpen());
  }

  @Test
  void aPresetsNameIsRefusedAsAPresetEvenBeforeTheTagsHaveArrived() {
    prompt.openForNewTag();
    type("Stone");

    assertFalse(prompt.confirm(Set.of()));
    assertEquals(TagNameOutcome.PRESET_COLLISION, prompt.refusal());
  }

  @Test
  void renamingATagToItsOwnNameIsAccepted() {
    prompt.openForRename("castle", "Castle");

    assertTrue(prompt.confirm(KNOWN));
  }

  @Test
  void editingTheTextClearsTheRefusal() {
    prompt.openForNewTag();
    type("Castle");
    prompt.confirm(KNOWN);

    prompt.charTyped('s');

    assertEquals(TagNameOutcome.OK, prompt.refusal());
    assertEquals("Castles", prompt.name());
  }

  @Test
  void deletingACharacterClearsTheRefusal() {
    prompt.openForNewTag();
    type("Castle");
    prompt.confirm(KNOWN);

    prompt.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false);

    assertEquals(TagNameOutcome.OK, prompt.refusal());
  }

  @Test
  void aKeyThatChangesNothingLeavesTheRefusalShowing() {
    prompt.openForNewTag();
    type("Castle");
    prompt.confirm(KNOWN);

    prompt.keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT, false);
    prompt.charTyped('/');

    assertEquals(TagNameOutcome.DUPLICATE, prompt.refusal());
  }

  @Test
  void closingAndReopeningForgetsTheRefusal() {
    prompt.openForNewTag();
    type("Castle");
    prompt.confirm(KNOWN);

    prompt.close();
    prompt.openForNewTag();

    assertEquals(TagNameOutcome.OK, prompt.refusal());
    assertEquals("", prompt.name());
  }
}
