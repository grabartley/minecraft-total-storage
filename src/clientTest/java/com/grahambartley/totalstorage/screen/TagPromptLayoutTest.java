package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import org.junit.jupiter.api.Test;

class TagPromptLayoutTest {
  private static final PromptFrame FRAME = TagPromptLayout.FRAME;
  private static final PromptListGeometry LIST = TagPromptLayout.LIST;

  @Test
  void thePromptSitsInsideTheTerminalWindow() {
    assertTrue(FRAME.left() >= 0 && FRAME.top() >= 0);
    assertTrue(FRAME.left() + FRAME.width() <= TerminalScreenLayout.WINDOW_WIDTH);
    assertTrue(FRAME.bottom() <= TerminalScreenLayout.WINDOW_HEIGHT);
  }

  @Test
  void theListStartsBelowTheHeaderWithAGap() {
    assertEquals(
        TagPromptLayout.headerTop() + TagPromptLayout.HEADER_HEIGHT + TagPromptLayout.HEADER_GAP,
        LIST.top());
  }

  @Test
  void theKeysSitBelowTheListAndInsideTheFrame() {
    assertTrue(TagPromptLayout.keysTop() > LIST.top() + LIST.trackHeight());
    assertTrue(
        TagPromptLayout.keysTop() + KeyCaps.HEIGHT <= FRAME.bottom() - PromptFrame.PADDING_BOTTOM);
  }

  @Test
  void theScrollbarSitsBesideTheRowsWithAGapAndInsideThePadding() {
    assertEquals(LIST.left() + LIST.rowWidth() + TagPromptLayout.SCROLLBAR_GAP, LIST.scrollbarX());
    assertEquals(
        FRAME.contentLeft() + PromptFrame.CONTENT_WIDTH, LIST.scrollbarX() + LIST.scrollbarWidth());
  }

  @Test
  void aClickInsideThePromptIsOverItAndOneBesideItIsNot() {
    assertTrue(TagPromptLayout.isOverPrompt(FRAME.left(), FRAME.top()));
    assertTrue(TagPromptLayout.isOverPrompt(FRAME.left() + FRAME.width() - 1, FRAME.bottom() - 1));
    assertTrue(!TagPromptLayout.isOverPrompt(FRAME.left() - 1, FRAME.top()));
    assertTrue(!TagPromptLayout.isOverPrompt(FRAME.left(), FRAME.bottom()));
  }

  @Test
  void aTickFitsInsideItsCheckBox() {
    assertTrue(TagPromptLayout.TICK_SIZE < TagPromptLayout.CHECK_SIZE);
    assertTrue(TagPromptLayout.CHECK_SIZE <= TagPromptLayout.ROW_HEIGHT);
  }
}
