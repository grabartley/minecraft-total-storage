package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Which container a click on the settings list landed on.
 *
 * <p>Rows are addressed by their place in the whole list rather than in the page, so a panel
 * scrolled down has to turn a screen position into the row a player is actually pointing at. Get it
 * wrong and a click excludes the wrong chest, which is the one mistake this panel must not make.
 */
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
                        index))
            .toList());
  }

  /** The list only exists on the containers section, so every case here opens it first. */
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

  /** Scrolled down, the same screen row is a different container, which is the whole hazard. */
  @Test
  void aClickOnARowFollowsWhereTheListIsScrolledTo() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 3));
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

  /** The gutter belongs to the scrollbar, so a click there must not land on the row behind it. */
  @Test
  void aClickOnTheScrollbarIsNotAContainerRow() {
    assertTrue(
        containers()
                .containerRowAt(TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y)
            < 0);
  }

  /** A settings row has no gutter, so the same point there is a row like any other. */
  @Test
  void aClickAtTheSamePointOnTheTerminalSectionIsStillASetting() {
    assertEquals(
        TerminalSetting.RADIUS,
        new TerminalSettingsPanel()
            .settingAt(TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y));
  }

  /** The terminal section holds no list, so a click there is never a container row. */
  @Test
  void theTerminalSectionHasNoContainerRows() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertTrue(
        panel.containerRowAt(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(0)) < 0);
    assertEquals(SettingsSection.TERMINAL, panel.section());
  }

  /** And the containers section holds no settings, so a click there is never a setting row. */
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

  /** Opening a section starts it at the top, so a list is never opened part way down. */
  @Test
  void switchingSectionReturnsTheListToItsTop() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5));
    panel.scroll().scrollBy(3);

    panel.show(SettingsSection.TERMINAL);
    panel.show(SettingsSection.CONTAINERS);

    assertEquals(0, panel.scroll().firstIndex());
  }

  /**
   * Only the container list scrolls, and the panel says so rather than leaving the scrollbar to
   * refuse on its own. It refuses by asking whether it overflows, which is the other section's
   * count until the next answer arrives, so for those frames a click on the radius stepper would be
   * taken as a grab on a scrollbar that is not drawn.
   */
  @Test
  void onlyTheContainerListScrolls() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5));

    assertTrue(panel.scrolls());
    panel.show(SettingsSection.TERMINAL);
    assertFalse(panel.scrolls());
    assertTrue(
        panel.scroll().overflows(),
        "with a stale count still overflowing, the section is the only thing refusing the grab");
  }

  /**
   * Every way in agrees about which section has a scrollbar, so no one of them can be the one that
   * forgets. A drag begun on the list and carried into the terminal section stops there.
   */
  @Test
  void noWayInReachesAScrollbarTheOpenSectionIsNotShowing() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5));
    int onTheThumb = TerminalSettingsLayout.CONTENT_Y + 1;

    assertTrue(panel.isOverScrollbar(TerminalScreenLayout.SCROLLBAR_X, onTheThumb));
    assertTrue(panel.grabScrollbar(onTheThumb));
    assertTrue(panel.isDraggingScrollbar());

    panel.show(SettingsSection.TERMINAL);

    assertFalse(panel.isOverScrollbar(TerminalScreenLayout.SCROLLBAR_X, onTheThumb));
    assertFalse(panel.grabScrollbar(onTheThumb));
    assertFalse(panel.isDraggingScrollbar());
  }

  /** The stepper that raises the radius sits in the gutter, and must not be taken for a grab. */
  @Test
  void aClickOnTheRadiusStepperIsNotAGrabOnTheScrollbar() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5));
    int rightmost = SettingsControl.radiusUpX() + SettingsControl.size() - 1;

    assertFalse(panel.isOverScrollbar(rightmost, SettingsControl.radiusY()));
    assertFalse(panel.grabScrollbar(SettingsControl.radiusY()));
  }

  /** A settings row is not a container row, however far along the panel the click landed. */
  @Test
  void aClickOnTheRadiusStepperIsOverNoContainerRow() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        panel.containerRowAt(
            SettingsControl.radiusUpX() + SettingsControl.size() - 1, SettingsControl.radiusY()));
  }

  /** A list that shrank under a scrolled panel must not leave it pointing past the end. */
  @Test
  void theScrollIsBroughtBackInsideAListThatShrank() {
    TerminalSettingsPanel panel = containers();
    panel.fit(viewOf(TerminalSettingsLayout.ROWS + 5));
    panel.scroll().scrollBy(5);

    panel.fit(viewOf(1));

    assertEquals(0, panel.scroll().firstIndex());
  }
}
