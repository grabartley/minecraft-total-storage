package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TagPromptLayoutTest {
  @Test
  void thePromptSitsOverTheGridItWasOpenedFrom() {
    assertTrue(TagPromptLayout.left() >= TerminalScreenLayout.GRID_X);
    assertTrue(TagPromptLayout.top() >= TerminalScreenLayout.GRID_Y);
    assertTrue(
        TagPromptLayout.left() + TagPromptLayout.WIDTH
            <= TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH);
    assertTrue(
        TagPromptLayout.top() + TagPromptLayout.HEIGHT
            <= TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT);
  }

  @Test
  void everyRowFitsBetweenTheHeadingAndTheFooter() {
    assertTrue(
        TagPromptLayout.rowY(TagPromptLayout.VISIBLE_ROWS - 1) + TagPromptLayout.ROW_HEIGHT
            <= TagPromptLayout.top() + TagPromptLayout.HEIGHT - TagPromptLayout.FOOTER_HEIGHT);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 100})
  void anIndexThatIsNotARowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TagPromptLayout.rowY(row));
  }

  @Test
  void aPointInsideARowResolvesToThatRow() {
    for (int row = 0; row < TagPromptLayout.VISIBLE_ROWS; row++) {
      assertEquals(
          row,
          TagPromptLayout.rowAt(
              TagPromptLayout.left() + TagPromptLayout.PADDING, TagPromptLayout.rowY(row)));
    }
  }

  @Test
  void aPointBesideTheRowsIsNotARow() {
    assertEquals(
        TagPromptLayout.NO_ROW,
        TagPromptLayout.rowAt(TagPromptLayout.left() - 1, TagPromptLayout.rowsTop()));
    assertEquals(
        TagPromptLayout.NO_ROW,
        TagPromptLayout.rowAt(
            TagPromptLayout.left() + TagPromptLayout.PADDING, TagPromptLayout.rowsTop() - 1));
  }

  @Test
  void thePromptRegionCoversTheWholePanelAndNothingOutsideIt() {
    assertTrue(TagPromptLayout.isOverPrompt(TagPromptLayout.left(), TagPromptLayout.top()));
    assertTrue(
        TagPromptLayout.isOverPrompt(
            TagPromptLayout.left() + TagPromptLayout.WIDTH - 1,
            TagPromptLayout.top() + TagPromptLayout.HEIGHT - 1));
    assertFalse(TagPromptLayout.isOverPrompt(TagPromptLayout.left() - 1, TagPromptLayout.top()));
    assertFalse(
        TagPromptLayout.isOverPrompt(
            TagPromptLayout.left(), TagPromptLayout.top() + TagPromptLayout.HEIGHT));
  }
}
