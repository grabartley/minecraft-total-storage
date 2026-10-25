package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalSettingsPanelTest {
  private static NetworkSettingsView viewOf(int total) {
    return NetworkSettingsView.answered(
        16,
        16,
        32,
        TerminalAccessMode.OWNER,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        total,
        IntStream.range(0, Math.min(total, TerminalSettingsLayout.ROWS))
            .mapToObj(
                index ->
                    new ContainerRoleEntry(
                        new BlockPos(index, 64, 0),
                        "",
                        ContainerRole.GENERAL,
                        List.of(),
                        Optional.empty(),
                        0,
                        0L,
                        0,
                        27,
                        index))
            .toList());
  }

  private static TerminalSettingsPanel containers() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.show(SettingsSection.CONTAINERS);
    return panel;
  }

  static java.util.stream.Stream<Integer> everyRow() {
    return IntStream.range(0, TerminalSettingsLayout.ROWS).boxed();
  }

  private static int listX() {
    return TerminalSettingsLayout.LIST_X + 1;
  }

  @ParameterizedTest
  @MethodSource("everyRow")
  void aClickOnARowFindsThatRowWhileTheListIsAtTheTop(int row) {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);

    assertEquals(row, panel.containerRowAt(listX(), TerminalSettingsLayout.rowY(row) + 1));
  }

  @Test
  void aClickOnARowFollowsWhereTheListIsScrolledTo() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);
    panel.scroll().scrollBy(3);

    assertEquals(4, panel.containerRowAt(listX(), TerminalSettingsLayout.rowY(1) + 1));
  }

  @Test
  void aClickOutsideTheListFindsNoRow() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);

    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(TerminalSettingsLayout.LIST_X - 1, TerminalSettingsLayout.LIST_Y));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(listX(), TerminalSettingsLayout.LIST_Y - 1));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(TerminalSettingsLayout.SCROLLBAR_X, TerminalSettingsLayout.LIST_Y));
  }

  @Test
  void theTerminalSectionHasNoContainerRows() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(20), 0);

    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(listX(), TerminalSettingsLayout.rowY(0) + 1));
  }

  @Test
  void theScrollbarIsOnlyThereForASectionThatScrolls() {
    final TerminalSettingsPanel terminal = new TerminalSettingsPanel();
    final TerminalSettingsPanel listed = containers();

    assertFalse(
        terminal.isOverScrollbar(
            TerminalSettingsLayout.SCROLLBAR_X, TerminalSettingsLayout.LIST_Y));
    assertTrue(
        listed.isOverScrollbar(TerminalSettingsLayout.SCROLLBAR_X, TerminalSettingsLayout.LIST_Y));
    assertFalse(
        listed.isOverScrollbar(
            TerminalSettingsLayout.SCROLLBAR_X - 1, TerminalSettingsLayout.LIST_Y));
  }

  @Test
  void theTagsScrollbarIsAsTallAsTheTagRowsAndNoTaller() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.show(SettingsSection.TAGS);
    final int bottom =
        TerminalSettingsLayout.LIST_Y
            + com.grahambartley.totalstorage.screenhandler.TagsSectionLayout.LIST_HEIGHT;

    assertTrue(panel.isOverScrollbar(TerminalSettingsLayout.SCROLLBAR_X, bottom - 1));
    assertFalse(panel.isOverScrollbar(TerminalSettingsLayout.SCROLLBAR_X, bottom));
  }

  @Test
  void switchingSectionReturnsTheListToItsTop() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);
    panel.scroll().scrollBy(5);

    panel.show(SettingsSection.TAGS);

    assertEquals(0, panel.scroll().firstIndex());
    assertEquals(SettingsSection.TAGS, panel.section());
  }

  @Test
  void onlyTheListSectionsScroll() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertFalse(panel.scrolls());
    panel.show(SettingsSection.CONTAINERS);
    assertTrue(panel.scrolls());
    panel.show(SettingsSection.TAGS);
    assertTrue(panel.scrolls());
  }

  @Test
  void theTerminalSectionNeverScrollsHoweverMuchItIsGiven() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(40), 40);

    assertFalse(panel.scroll().overflows());
  }

  @Test
  void theScrollIsBroughtBackInsideAListThatShrank() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);
    panel.scroll().scrollBy(10);

    panel.fit(viewOf(5), 0);

    assertEquals(1, panel.scroll().firstIndex());
  }

  @Test
  void theTagsSectionScrollsOverTheCustomTagsOnly() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.show(SettingsSection.TAGS);

    panel.fit(viewOf(40), TerminalSettingsLayout.ROWS);
    assertFalse(panel.scroll().overflows());

    panel.fit(viewOf(40), TerminalSettingsLayout.ROWS + 1);
    assertTrue(panel.scroll().overflows());
  }

  @Test
  void grabbingTheScrollbarOfAListThatFitsDoesNothing() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(2), 0);

    assertFalse(panel.grabScrollbar(TerminalSettingsLayout.LIST_Y + 1));
    assertFalse(panel.isDraggingScrollbar());
  }

  @Test
  void draggingTheScrollbarToTheBottomShowsTheLastContainers() {
    final TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(20), 0);

    assertTrue(panel.grabScrollbar(TerminalSettingsLayout.LIST_Y));
    panel.dragScrollbarTo(TerminalSettingsLayout.LIST_Y + TerminalSettingsLayout.LIST_HEIGHT + 40);

    assertEquals(20 - TerminalSettingsLayout.ROWS, panel.scroll().firstIndex());
    panel.releaseScrollbar();
    assertFalse(panel.isDraggingScrollbar());
  }
}
