package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.access.TrustEditOutcome;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.network.TerminalTrustReplyPayload;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lwjgl.glfw.GLFW;

class TrustPromptTest {
  private static final int ROW_RIGHT = TagPromptLayout.textLeft() + TagPromptLayout.ROW_WIDTH;

  private final TrustPrompt prompt = new TrustPrompt();

  private void type(String text) {
    for (char character : text.toCharArray()) {
      prompt.charTyped(character);
    }
  }

  private static NetworkSettingsView trusting(List<String> names) {
    return NetworkSettingsView.answered(
        4,
        4,
        16,
        TerminalAccessMode.TRUSTED,
        true,
        true,
        names,
        NetworkCapacity.EMPTY,
        0,
        0,
        List.of());
  }

  private static TerminalTrustReplyPayload reply(String name, TrustEditOutcome outcome) {
    return new TerminalTrustReplyPayload(1, name, outcome);
  }

  @Test
  void aNewPromptIsClosed() {
    assertFalse(prompt.isOpen());
    assertEquals("", prompt.name());
  }

  @Test
  void openingStartsWithAnEmptyNameAndNoRefusal() {
    prompt.open();

    assertTrue(prompt.isOpen());
    assertEquals("", prompt.name());
    assertNull(prompt.refusal());
  }

  @Test
  void typingWhileClosedDoesNothing() {
    assertEquals(PromptKey.HANDLED, prompt.charTyped('a'));
    assertEquals("", prompt.name());
  }

  @Test
  void aNameTheServerAddedIsClearedForTheNextOne() {
    prompt.open();
    type("Alex");

    prompt.answered(reply("alex", TrustEditOutcome.ADDED));

    assertEquals("", prompt.name());
    assertNull(prompt.refusal());
  }

  @ParameterizedTest
  @EnumSource(
      value = TrustEditOutcome.class,
      names = {"UNKNOWN_PLAYER", "ALREADY_TRUSTED"})
  void aRefusedNameStaysInThePromptAndSaysWhy(TrustEditOutcome outcome) {
    prompt.open();
    type("Nobody");

    prompt.answered(reply("Nobody", outcome));

    assertTrue(prompt.isOpen());
    assertEquals("Nobody", prompt.name());
    assertEquals(outcome, prompt.refusal());
  }

  @Test
  void editingTheNameForgetsTheRefusal() {
    prompt.open();
    type("Nobody");
    prompt.answered(reply("Nobody", TrustEditOutcome.UNKNOWN_PLAYER));

    prompt.keyPressed(GLFW.GLFW_KEY_BACKSPACE, false);

    assertNull(prompt.refusal());
  }

  @Test
  void aKeyThatChangesNothingKeepsTheRefusal() {
    prompt.open();
    type("Nobody");
    prompt.answered(reply("Nobody", TrustEditOutcome.UNKNOWN_PLAYER));

    prompt.keyPressed(GLFW.GLFW_KEY_ENTER, false);

    assertEquals(TrustEditOutcome.UNKNOWN_PLAYER, prompt.refusal());
  }

  @Test
  void aDeniedEditClosesThePrompt() {
    prompt.open();

    prompt.answered(reply("Alex", TrustEditOutcome.DENIED));

    assertFalse(prompt.isOpen());
  }

  @Test
  void aRemovalLeavesWhatIsBeingTypedAlone() {
    prompt.open();
    type("Sam");

    prompt.answered(reply("Alex", TrustEditOutcome.REMOVED));

    assertEquals("Sam", prompt.name());
    assertNull(prompt.refusal());
  }

  @Test
  void anAddedReplyForAnotherNameLeavesWhatIsBeingTypedAlone() {
    prompt.open();
    type("Sam");

    prompt.answered(reply("Alex", TrustEditOutcome.ADDED));

    assertEquals("Sam", prompt.name());
  }

  @Test
  void aReplyWhileClosedIsIgnored() {
    prompt.answered(reply("Alex", TrustEditOutcome.UNKNOWN_PLAYER));

    assertFalse(prompt.isOpen());
    assertNull(prompt.refusal());
  }

  @Test
  void closingForgetsTheName() {
    prompt.open();
    type("Alex");

    prompt.close();

    assertFalse(prompt.isOpen());
    assertEquals("", prompt.name());
  }

  @Test
  void theRemoveControlSitsAtTheRightEndOfEachRow() {
    assertTrue(TrustPrompt.isOverRemove(ROW_RIGHT - 1));
    assertTrue(TrustPrompt.isOverRemove(ROW_RIGHT - TrustPrompt.REMOVE_WIDTH));
    assertFalse(TrustPrompt.isOverRemove(ROW_RIGHT - TrustPrompt.REMOVE_WIDTH - 1));
    assertFalse(TrustPrompt.isOverRemove(ROW_RIGHT));
  }

  @Test
  void clickingRemoveOnARowNamesThePlayerOnIt() {
    NetworkSettingsView view = trusting(List.of("Alex", "Sam"));
    prompt.open();
    prompt.fit(view);

    assertEquals("Alex", prompt.removalAt(view, ROW_RIGHT - 1, TagPromptLayout.rowY(0) + 1));
    assertEquals("Sam", prompt.removalAt(view, ROW_RIGHT - 1, TagPromptLayout.rowY(1) + 1));
  }

  @Test
  void clickingANameRatherThanItsRemoveControlRemovesNobody() {
    NetworkSettingsView view = trusting(List.of("Alex"));
    prompt.open();
    prompt.fit(view);

    assertNull(prompt.removalAt(view, TagPromptLayout.textLeft() + 1, TagPromptLayout.rowY(0) + 1));
  }

  @Test
  void clickingRemoveOnAnEmptyRowRemovesNobody() {
    NetworkSettingsView view = trusting(List.of("Alex"));
    prompt.open();
    prompt.fit(view);

    assertNull(prompt.removalAt(view, ROW_RIGHT - 1, TagPromptLayout.rowY(1) + 1));
  }

  @Test
  void aLongListScrollsToReachTheRestOfTheNames() {
    NetworkSettingsView view = trusting(List.of("A", "B", "C", "D", "E", "F"));
    prompt.open();
    prompt.fit(view);

    assertTrue(prompt.list().scrollBy(1));
    assertEquals("B", prompt.removalAt(view, ROW_RIGHT - 1, TagPromptLayout.rowY(0) + 1));
  }
}
