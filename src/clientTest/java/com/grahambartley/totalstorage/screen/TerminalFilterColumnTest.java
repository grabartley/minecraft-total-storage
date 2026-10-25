package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
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
      entries.add(new NetworkTagEntry("tag" + tag, "Tag " + tag, true, 0, tag));
    }
    return new NetworkTagView(true, entries, Optional.empty(), Set.of());
  }

  private static int rowX() {
    return FilterColumnLayout.X + 1;
  }

  private boolean clickRow(NetworkTagView view, int row) {
    return column.click(selection, rowX(), FilterColumnLayout.rowY(row), GridClick.LEFT_BUTTON);
  }

  @Test
  void clickingATagFiltersOnItAndClickingAgainClearsIt() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertTrue(clickRow(view, 0));
    assertTrue(selection.isActive("tag0"));
    assertTrue(clickRow(view, 0));
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
  void aClickOutsideTheColumnIsLeftToTheScreen() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertFalse(
        column.click(
            selection, FilterColumnLayout.X - 5, FilterColumnLayout.Y, GridClick.LEFT_BUTTON));
  }

  @Test
  void onlyTheLeftButtonWorksTheColumn() {
    final NetworkTagView view = viewOf(3);
    column.fit(view);

    assertFalse(
        column.click(selection, rowX(), FilterColumnLayout.rowY(0), GridClick.RIGHT_BUTTON));
    assertFalse(selection.isFiltering());
  }

  @Test
  void aRowBeyondTheTagsThereAreSelectsNothing() {
    final NetworkTagView view = viewOf(1);
    column.fit(view);

    assertTrue(clickRow(view, FilterColumnLayout.ROWS - 1));
    assertFalse(selection.isFiltering());
  }

  @Test
  void scrollingTheColumnMovesWhichTagARowStandsFor() {
    final NetworkTagView view = viewOf(FilterColumnLayout.ROWS + 5);
    column.fit(view);
    column.scroll().scrollBy(2);

    final NetworkTagEntry tag = column.tagAt(rowX(), FilterColumnLayout.rowY(0));

    assertEquals("tag2", tag.id());
  }

  @Test
  void aColumnShorterThanItsRowsDoesNotScroll() {
    final NetworkTagView view = viewOf(2);
    column.fit(view);

    assertFalse(column.scroll().overflows());
    assertNull(column.tagAt(rowX(), FilterColumnLayout.rowY(FilterColumnLayout.ROWS - 1)));
  }

  @Test
  void yourOwnTagsAreListedAfterThePresetsBelowADivider() {
    final NetworkTagView view =
        new NetworkTagView(
            true,
            List.of(
                new NetworkTagEntry("castle", "Castle", false, 1, 4),
                new NetworkTagEntry("wood", "tag.wood", true, 0, 9)),
            Optional.empty(),
            Set.of());
    column.fit(view);

    assertEquals("wood", column.tagAt(rowX(), FilterColumnLayout.rowY(0)).id());
    assertNull(column.tagAt(rowX(), FilterColumnLayout.rowY(1)));
    assertEquals("castle", column.tagAt(rowX(), FilterColumnLayout.rowY(2)).id());
  }

  @Test
  void clickingTheDividerFiltersNothing() {
    final NetworkTagView view =
        new NetworkTagView(
            true,
            List.of(new NetworkTagEntry("castle", "Castle", false, 1, 4)),
            Optional.empty(),
            Set.of());
    column.fit(view);

    assertTrue(clickRow(view, 0));
    assertFalse(selection.isFiltering());
  }

  @Test
  void aClickOnTheScrollbarGrabsItRatherThanFiltering() {
    final NetworkTagView view = viewOf(FilterColumnLayout.ROWS + 5);
    column.fit(view);

    assertTrue(
        column.click(
            selection,
            FilterColumnLayout.SCROLLBAR_X,
            FilterColumnLayout.CONTENT_Y + 1,
            GridClick.LEFT_BUTTON));
    assertTrue(column.isDraggingScrollbar());
    assertFalse(selection.isFiltering());
  }
}
