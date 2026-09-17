package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.screenhandler.TagsSectionLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalSettingsPanelTest {
  private static NetworkSettingsView viewOf(int total) {
    return new NetworkSettingsView(
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
                        ContainerRole.GENERAL,
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

  @ParameterizedTest
  @MethodSource("everyRow")
  void aClickOnARowFindsThatRowWhileTheListIsAtTheTop(int row) {
    TerminalSettingsPanel panel = containers();

    assertEquals(
        row,
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(row)));
  }

  @Test
  void aClickOnARowFollowsWhereTheListIsScrolledTo() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 3), 0);
    panel.scroll().scrollBy(2);

    assertEquals(
        2, panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)));
  }

  @Test
  void aClickOutsideTheListFindsNoRow() {
    TerminalSettingsPanel panel = containers();

    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.CONTENT_Y - 1)
            < 0);
    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X - 1, TerminalSettingsLayout.CONTENT_Y)
            < 0);
  }

  @Test
  void aClickOnTheScrollbarIsNotAContainerRow() {
    assertTrue(
        containers()
                .containerRowAt(TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y)
            < 0);
  }

  @Test
  void aClickAtTheSamePointOnTheTerminalSectionIsStillASetting() {
    assertEquals(
        TerminalSetting.RADIUS,
        new TerminalSettingsPanel()
            .settingAt(TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y));
  }

  @Test
  void theTerminalSectionHasNoContainerRows() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)) < 0);
    assertEquals(SettingsSection.TERMINAL, panel.section());
  }

  @Test
  void theContainersSectionHasNoSettingRows() {
    assertEquals(
        null,
        containers().settingAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)));
  }

  @ParameterizedTest
  @EnumSource(TerminalSetting.class)
  void everySettingIsFoundWhereItsRowIsDrawn(TerminalSetting setting) {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertEquals(
        setting,
        panel.settingAt(
            TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(setting.row())));
  }

  @Test
  void aRowUnderTheNoticeStripCannotBeClicked() {
    TerminalSettingsPanel panel = containers();

    panel.coverTopRows(2);

    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)) < 0);
    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(1)) < 0);
    assertEquals(
        2, panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(2)));
  }

  @Test
  void aSettingUnderTheNoticeStripCannotBeClicked() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    panel.coverTopRows(1);

    assertEquals(
        null,
        panel.settingAt(
            TerminalSettingsLayout.PANEL_X,
            TerminalSettingsLayout.rowY(TerminalSetting.RADIUS.row())));
    assertEquals(
        TerminalSetting.ACCESS,
        panel.settingAt(
            TerminalSettingsLayout.PANEL_X,
            TerminalSettingsLayout.rowY(TerminalSetting.ACCESS.row())));
  }

  @Test
  void aNoticeTallerThanThePanelCoversNoMoreRowsThanThereAre() {
    TerminalSettingsPanel panel = containers();

    panel.coverTopRows(Integer.MAX_VALUE);

    assertEquals(TerminalSettingsLayout.ROWS, panel.hiddenRows());
  }

  @Test
  void aNoticeThatHasGoneUncoversTheRowsAgain() {
    TerminalSettingsPanel panel = containers();
    panel.coverTopRows(2);

    panel.coverTopRows(0);

    assertEquals(
        0, panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)));
  }

  @Test
  void switchingSectionReturnsTheListToItsTop() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5), 0);
    panel.scroll().scrollBy(3);

    panel.show(SettingsSection.TERMINAL);
    panel.show(SettingsSection.CONTAINERS);

    assertEquals(0, panel.scroll().firstIndex());
  }

  @Test
  void onlyTheContainerListScrolls() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5), 0);

    assertTrue(panel.scrolls());
    panel.show(SettingsSection.TERMINAL);
    assertFalse(panel.scrolls());
    assertTrue(
        panel.scroll().overflows(),
        "with a stale count still overflowing, the section is the only thing refusing the grab");
  }

  @Test
  void noWayInReachesAScrollbarTheOpenSectionIsNotShowing() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5), 0);
    int onTheThumb = TerminalSettingsLayout.CONTENT_Y + 1;

    assertTrue(panel.isOverScrollbar(TerminalScreenLayout.SCROLLBAR_X, onTheThumb));
    assertTrue(panel.grabScrollbar(onTheThumb));
    assertTrue(panel.isDraggingScrollbar());

    panel.show(SettingsSection.TERMINAL);

    assertFalse(panel.isOverScrollbar(TerminalScreenLayout.SCROLLBAR_X, onTheThumb));
    assertFalse(panel.grabScrollbar(onTheThumb));
    assertFalse(panel.isDraggingScrollbar());
  }

  @Test
  void aClickOnTheRadiusStepperIsNotAGrabOnTheScrollbar() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5), 0);
    int rightmost = SettingsControl.radiusUpX() + SettingsControl.size() - 1;

    assertFalse(panel.isOverScrollbar(rightmost, SettingsControl.radiusY()));
    assertFalse(panel.grabScrollbar(SettingsControl.radiusY()));
  }

  @Test
  void aClickOnTheRadiusStepperIsOverNoContainerRow() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(
            SettingsControl.radiusUpX() + SettingsControl.size() - 1, SettingsControl.radiusY()));
  }

  @Test
  void theScrollIsBroughtBackInsideAListThatShrank() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5), 0);
    panel.scroll().scrollBy(5);

    panel.fit(viewOf(1), 0);

    assertEquals(0, panel.scroll().firstIndex());
  }

  private static TerminalSettingsPanel tags() {
    final TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.show(SettingsSection.TAGS);
    return panel;
  }

  @Test
  void theLastCustomTagCanBeReachedByScrolling() {
    final int total = TagsSectionLayout.TAG_ROWS + 5;
    TerminalSettingsPanel panel = tags();
    panel.fit(viewOf(0), total);

    panel.scroll().scrollTo(panel.scroll().maxTopRow());

    assertEquals(
        total - 1,
        panel.scroll().firstIndex() + TagsSectionLayout.TAG_ROWS - 1,
        "the last tag can never be drawn, so it can never be renamed or deleted");
  }

  @Test
  void aTagListThatFitsDoesNotScroll() {
    TerminalSettingsPanel panel = tags();

    panel.fit(viewOf(0), TagsSectionLayout.TAG_ROWS);

    assertFalse(panel.scroll().overflows());
    assertEquals(0, panel.scroll().maxTopRow());
  }

  @Test
  void oneTagMoreThanFitsStartsScrolling() {
    TerminalSettingsPanel panel = tags();

    panel.fit(viewOf(0), TagsSectionLayout.TAG_ROWS + 1);

    assertTrue(panel.scroll().overflows());
  }

  @Test
  void theNewTagRowIsCountedSoTheLastTagStillReaches() {
    TerminalSettingsPanel panel = tags();

    panel.fit(viewOf(0), TagsSectionLayout.TAG_ROWS + 1);

    assertEquals(
        TagsSectionLayout.FIRST_TAG_ROW,
        panel.scroll().totalRows() - (TagsSectionLayout.TAG_ROWS + 1),
        "the new tag row has to be counted or the last tag falls off the end");
  }

  @Test
  void aNetworkWithNoCustomTagsScrollsNowhere() {
    TerminalSettingsPanel panel = tags();

    panel.fit(viewOf(0), 0);

    assertFalse(panel.scroll().overflows());
  }

  @Test
  void theTagsSectionScrollsWhileTheTerminalSectionDoesNot() {
    assertTrue(tags().scrolls());
    assertFalse(new TerminalSettingsPanel().scrolls());
  }

  @Test
  void theTerminalSectionNeverScrollsHoweverMuchItIsGiven() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 50), 50);

    assertFalse(panel.scroll().overflows());
  }
}
