package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.OwnedTerminal;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class TagSourcePromptTest {
  private static final OwnedTerminal FIRST = new OwnedTerminal(new BlockPos(1, 1, 1), 2);
  private static final OwnedTerminal SECOND = new OwnedTerminal(new BlockPos(2, 2, 2), 3);
  private static final List<OwnedTerminal> BOTH = List.of(FIRST, SECOND);

  @Test
  void aFreshPromptIsClosedAndArmedAtNothing() {
    final TagSourcePrompt prompt = new TagSourcePrompt();

    assertFalse(prompt.isOpen());
    assertFalse(prompt.isArmed(FIRST));
  }

  @Test
  void openingShowsThePromptAndClosingHidesItAgain() {
    final TagSourcePrompt prompt = new TagSourcePrompt();

    prompt.open();
    assertTrue(prompt.isOpen());

    prompt.close();
    assertFalse(prompt.isOpen());
  }

  @Test
  void armingOneSourceLeavesEveryOtherSourceUnarmed() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();

    prompt.armForReplace(FIRST);

    assertTrue(prompt.isArmed(FIRST));
    assertFalse(prompt.isArmed(SECOND));
  }

  @Test
  void aPromptThatClosesForgetsWhatWasArmedForReplacement() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();
    prompt.armForReplace(FIRST);

    prompt.close();
    prompt.open();

    assertFalse(prompt.isArmed(FIRST));
  }

  @Test
  void disarmingClearsTheArmedSource() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();
    prompt.armForReplace(FIRST);

    prompt.disarm();

    assertFalse(prompt.isArmed(FIRST));
  }

  @Test
  void nothingIsEverArmedByAnAbsentSource() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();

    assertFalse(prompt.isArmed(null));
  }

  @Test
  void aPointOutsideTheRowsNamesNoSource() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();
    prompt.fit(BOTH);

    assertNull(prompt.sourceAt(BOTH, TagPromptLayout.LIST.left() - 5, TagPromptLayout.LIST.top()));
  }

  @Test
  void theFirstRowNamesTheFirstSourceTheServerSent() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();
    prompt.fit(BOTH);

    assertEquals(
        FIRST,
        prompt.sourceAt(BOTH, TagPromptLayout.LIST.left() + 1, TagPromptLayout.LIST.rowY(0) + 1));
  }

  @Test
  void aRowPastTheEndOfTheListNamesNoSource() {
    final TagSourcePrompt prompt = new TagSourcePrompt();
    prompt.open();
    prompt.fit(List.of(FIRST));

    assertNull(
        prompt.sourceAt(
            List.of(FIRST), TagPromptLayout.LIST.left() + 1, TagPromptLayout.LIST.rowY(1) + 1));
  }
}
