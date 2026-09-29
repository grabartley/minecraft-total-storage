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
  void thePromptSitsInsideTheItemGridInsetItWasOpenedFrom() {
    assertTrue(TagPromptLayout.left() >= TerminalScreenLayout.INSET_X);
    assertTrue(TagPromptLayout.top() >= TerminalScreenLayout.INSET_Y);
    assertTrue(
        TagPromptLayout.left() + TagPromptLayout.WIDTH
            <= TerminalScreenLayout.INSET_X + TerminalScreenLayout.INSET_WIDTH);
    assertTrue(
        TagPromptLayout.top() + TagPromptLayout.HEIGHT
            <= TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT);
  }

  @Test
  void everyRowFitsBetweenTheHeadingAndTheFooter() {
    assertTrue(
        TagPromptLayout.rowsTop() >= TagPromptLayout.headingTop() + TagPromptLayout.LINE_HEIGHT);
    assertTrue(
        TagPromptLayout.rowY(TagPromptLayout.VISIBLE_ROWS - 1) + TagPromptLayout.ROW_HEIGHT
            <= TagPromptLayout.footerTop());
  }

  @Test
  void theFooterHasRoomForItsLinesInsideThePanel() {
    assertEquals(2, TagPromptLayout.FOOTER_LINES);
    assertTrue(
        TagPromptLayout.footerTop() + TagPromptLayout.FOOTER_LINES * TagPromptLayout.LINE_HEIGHT
            <= TagPromptLayout.top() + TagPromptLayout.HEIGHT - TagPromptLayout.PADDING);
  }

  @Test
  void theScrollbarRunsBesideTheRowsInsideThePanel() {
    assertTrue(
        TagPromptLayout.scrollbarX() >= TagPromptLayout.textLeft() + TagPromptLayout.ROW_WIDTH);
    assertTrue(
        TagPromptLayout.scrollbarX() + TagPromptLayout.SCROLLBAR_WIDTH
            <= TagPromptLayout.left() + TagPromptLayout.WIDTH - TagPromptLayout.PADDING);
    assertEquals(
        TagPromptLayout.VISIBLE_ROWS * TagPromptLayout.ROW_HEIGHT, TagPromptLayout.TRACK_HEIGHT);
  }

  @Test
  void aPointOnTheScrollbarIsNotARowAndAPointOnARowIsNotTheScrollbar() {
    int trackY = TagPromptLayout.rowsTop();
    assertTrue(TagPromptLayout.isOverScrollbar(TagPromptLayout.scrollbarX(), trackY));
    assertEquals(
        TagPromptLayout.NO_ROW, TagPromptLayout.rowAt(TagPromptLayout.scrollbarX(), trackY));
    assertFalse(TagPromptLayout.isOverScrollbar(TagPromptLayout.textLeft(), trackY));
  }

  @Test
  void theScrollbarEndsWhereTheRowsEnd() {
    int x = TagPromptLayout.scrollbarX();
    assertFalse(TagPromptLayout.isOverScrollbar(x, TagPromptLayout.rowsTop() - 1));
    assertTrue(
        TagPromptLayout.isOverScrollbar(
            x, TagPromptLayout.rowsTop() + TagPromptLayout.TRACK_HEIGHT - 1));
    assertFalse(
        TagPromptLayout.isOverScrollbar(
            x, TagPromptLayout.rowsTop() + TagPromptLayout.TRACK_HEIGHT));
    assertFalse(
        TagPromptLayout.isOverScrollbar(
            x + TagPromptLayout.SCROLLBAR_WIDTH, TagPromptLayout.rowsTop()));
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
          row, TagPromptLayout.rowAt(TagPromptLayout.textLeft(), TagPromptLayout.rowY(row)));
    }
  }

  @Test
  void aPointBesideTheRowsIsNotARow() {
    assertEquals(
        TagPromptLayout.NO_ROW,
        TagPromptLayout.rowAt(TagPromptLayout.left() - 1, TagPromptLayout.rowsTop()));
    assertEquals(
        TagPromptLayout.NO_ROW,
        TagPromptLayout.rowAt(TagPromptLayout.textLeft(), TagPromptLayout.rowsTop() - 1));
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
