package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.filter.FilterSelection;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.screenhandler.FilterColumnLayout;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TerminalFilterColumnTest {
  private final TerminalFilterColumn column = new TerminalFilterColumn();
  private final FilterSelection selection = new FilterSelection();

  private static NetworkTagView viewOf(int tags) {
    final List<NetworkTagEntry> entries = new ArrayList<>();
    for (int tag = 0; tag < tags; tag++) {
      entries.add(new NetworkTagEntry("tag" + tag, "Tag " + tag, false, 0, tag));
    }
    return new NetworkTagView(true, entries, Optional.empty(), Set.of());
  }

  private static int rowX() {
    return FilterColumnLayout.X + 1;
  }

  private FilterColumnClick clickRow(NetworkTagView view, int row) {
    return column.click(
        view, selection, rowX(), FilterColumnLayout.rowY(row), GridClick.LEFT_BUTTON);
  }

  @Test
  void clickingATagFiltersOnItAndClickingAgainClearsIt() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertEquals(FilterColumnClick.FILTER_CHANGED, clickRow(view, 0));
    assertTrue(selection.isActive("tag0"));
    assertEquals(FilterColumnClick.FILTER_CHANGED, clickRow(view, 0));
    assertFalse(selection.isActive("tag0"));
  }

  @Test
  void severalTagsCanBeActiveAtOnce() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    clickRow(view, 0);
    clickRow(view, 1);

    assertEquals(Set.of("tag0", "tag1"), selection.active());
  }

  @Test
  void theClearControlDropsEveryFilterInOneAction() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);
    clickRow(view, 0);
    clickRow(view, 1);

    assertEquals(
        FilterColumnClick.FILTER_CHANGED,
        column.click(
            view,
            selection,
            FilterColumnLayout.CLEAR_X + 1,
            FilterColumnLayout.Y + 1,
            GridClick.LEFT_BUTTON));
    assertFalse(selection.isFiltering());
  }

  @Test
  void clearingWhenNothingIsFilteredCostsNoRequest() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertEquals(
        FilterColumnClick.HANDLED,
        column.click(
            view,
            selection,
            FilterColumnLayout.CLEAR_X + 1,
            FilterColumnLayout.Y + 1,
            GridClick.LEFT_BUTTON));
  }

  @Test
  void theModeControlSwapsBetweenNarrowingAndBroadening() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);
    clickRow(view, 0);

    assertEquals(
        FilterColumnClick.FILTER_CHANGED,
        column.click(
            view,
            selection,
            FilterColumnLayout.X + 1,
            FilterColumnLayout.Y + 1,
            GridClick.LEFT_BUTTON));
    assertEquals(TagCombinationMode.OR, selection.mode());
  }

  @Test
  void changingTheModeWithNothingSelectedChangesNoResults() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertEquals(
        FilterColumnClick.HANDLED,
        column.click(
            view,
            selection,
            FilterColumnLayout.X + 1,
            FilterColumnLayout.Y + 1,
            GridClick.LEFT_BUTTON));
    assertEquals(TagCombinationMode.OR, selection.mode());
  }

  @Test
  void aClickOutsideTheColumnIsLeftToTheScreen() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertEquals(
        FilterColumnClick.IGNORED,
        column.click(
            view,
            selection,
            FilterColumnLayout.X - 5,
            FilterColumnLayout.Y,
            GridClick.LEFT_BUTTON));
  }

  @Test
  void onlyTheLeftButtonWorksTheColumn() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertEquals(
        FilterColumnClick.IGNORED,
        column.click(view, selection, rowX(), FilterColumnLayout.rowY(0), GridClick.RIGHT_BUTTON));
    assertFalse(selection.isFiltering());
  }

  @Test
  void aRowBeyondTheTagsThereAreSelectsNothing() {
    final NetworkTagView view = viewOf(1);
    column.fit(view);

    assertEquals(FilterColumnClick.HANDLED, clickRow(view, FilterColumnLayout.ROWS - 1));
    assertFalse(selection.isFiltering());
  }

  @Test
  void scrollingTheColumnMovesWhichTagARowStandsFor() {
    final NetworkTagView view = viewOf(FilterColumnLayout.ROWS + 5);
    column.fit(view);
    column.scroll().scrollBy(2);

    final NetworkTagEntry tag = column.tagAt(view, rowX(), FilterColumnLayout.rowY(0));

    assertEquals("tag2", tag.id());
  }

  @Test
  void aColumnShorterThanItsRowsDoesNotScroll() {
    final NetworkTagView view = viewOf(2);
    column.fit(view);

    assertFalse(column.scroll().overflows());
    assertNull(column.tagAt(view, rowX(), FilterColumnLayout.rowY(FilterColumnLayout.ROWS - 1)));
  }

  @Test
  void aClickOnTheScrollbarGrabsItRatherThanFiltering() {
    final NetworkTagView view = viewOf(FilterColumnLayout.ROWS + 5);
    column.fit(view);

    assertEquals(
        FilterColumnClick.SCROLLBAR_GRABBED,
        column.click(
            view,
            selection,
            FilterColumnLayout.SCROLLBAR_X,
            FilterColumnLayout.CONTENT_Y + 1,
            GridClick.LEFT_BUTTON));
    assertTrue(column.isDraggingScrollbar());
    assertFalse(selection.isFiltering());
  }
}
