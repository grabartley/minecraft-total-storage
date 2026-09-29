package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TagPromptListTest {
  private static final int PRESET_TAGS = 13;
  private static final int ROW_X = TagPromptLayout.textLeft();
  private static final int BAR_X = TagPromptLayout.scrollbarX();
  private static final int TRACK_TOP = TagPromptLayout.rowsTop();
  private static final int TRACK_BOTTOM = TRACK_TOP + TagPromptLayout.TRACK_HEIGHT - 1;

  private final TagPromptList list = new TagPromptList();

  @Test
  void aListThatFitsOffersNoScrollbarToGrab() {
    list.fit(TagPromptLayout.VISIBLE_ROWS);

    assertFalse(list.grabScrollbar(BAR_X, TRACK_TOP));
    assertFalse(list.isDraggingScrollbar());
  }

  @Test
  void aFreshNetworksTagsOfferAScrollbarToGrab() {
    list.fit(PRESET_TAGS);

    assertTrue(list.grabScrollbar(BAR_X, TRACK_TOP));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3})
  void eachRowNamesTheEntryScrolledToIt(int row) {
    list.fit(PRESET_TAGS);
    list.scrollBy(2);

    assertEquals(2 + row, list.indexAt(ROW_X, TagPromptLayout.rowY(row)));
    assertEquals(2 + row, list.indexOfRow(row));
  }

  @Test
  void aPointOffTheRowsNamesNoEntry() {
    list.fit(PRESET_TAGS);

    assertEquals(TagPromptLayout.NO_ROW, list.indexAt(BAR_X, TRACK_TOP));
    assertEquals(TagPromptLayout.NO_ROW, list.indexAt(ROW_X, TRACK_TOP - 1));
  }

  @Test
  void aGrabOffTheScrollbarIsNotAGrab() {
    list.fit(PRESET_TAGS);

    assertFalse(list.grabScrollbar(ROW_X, TRACK_TOP));
    assertFalse(list.isDraggingScrollbar());
  }

  @Test
  void draggingTheThumbToTheBottomScrollsToTheLastRowsAndMovesTheThumb() {
    list.fit(PRESET_TAGS);
    int thumbAtTop = list.thumbTop();

    assertTrue(list.grabScrollbar(BAR_X, TRACK_TOP));
    list.dragScrollbarTo(TRACK_BOTTOM + 40);

    assertTrue(list.isDraggingScrollbar());
    assertEquals(PRESET_TAGS - TagPromptLayout.VISIBLE_ROWS, list.indexOfRow(0));
    assertTrue(list.thumbTop() > thumbAtTop);
  }

  @Test
  void theWheelMovesTheThumbToo() {
    list.fit(PRESET_TAGS);
    int thumbAtTop = list.thumbTop();

    assertTrue(list.scrollBy(3));

    assertTrue(list.thumbTop() > thumbAtTop);
  }

  @Test
  void releasingTheThumbEndsTheDrag() {
    list.fit(PRESET_TAGS);
    list.grabScrollbar(BAR_X, TRACK_TOP);

    list.releaseScrollbar();

    assertFalse(list.isDraggingScrollbar());
  }

  @Test
  void resettingReturnsToTheTopAndDropsAnyDrag() {
    list.fit(PRESET_TAGS);
    list.grabScrollbar(BAR_X, TRACK_BOTTOM);

    list.reset();

    assertFalse(list.isDraggingScrollbar());
    assertEquals(0, list.indexOfRow(0));
    assertEquals(0, list.thumbTop());
  }
}
