package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalSettingsButtonsTest {
  private static final int ORIGIN_X = 100;
  private static final int ORIGIN_Y = 60;
  private static final int MAX_RADIUS = 64;

  private final List<ClickableWidget> added = new ArrayList<>();
  private TerminalSettingsButtons.Edits edits;
  private TerminalSettingsButtons buttons;

  private static NetworkSettingsView view(
      int radius, TerminalAccessMode mode, boolean mayAdminister, List<String> trusted) {
    return NetworkSettingsView.answered(
        radius,
        radius,
        MAX_RADIUS,
        mode,
        mayAdminister,
        true,
        trusted,
        NetworkCapacity.EMPTY,
        0,
        0,
        List.of());
  }

  private static NetworkSettingsView owned(int radius) {
    return view(radius, TerminalAccessMode.OWNER, true, List.of());
  }

  @BeforeEach
  void setUp() {
    edits = mock(TerminalSettingsButtons.Edits.class);
    buttons = new TerminalSettingsButtons(added::add, ORIGIN_X, ORIGIN_Y, edits);
  }

  private ClickableWidget access() {
    return added.get(0);
  }

  private ClickableWidget trusted() {
    return added.get(1);
  }

  private ClickableWidget radiusDown() {
    return added.get(2);
  }

  private ClickableWidget radiusUp() {
    return added.get(3);
  }

  private static String keyOf(ClickableWidget button) {
    return ((TranslatableTextContent) button.getMessage().getContent()).getKey();
  }

  @Test
  void theAccessAndTrustedButtonsLineUpWithTheirSettingRows() {
    assertEquals(4, added.size());
    assertEquals(ORIGIN_X + TerminalSettingsLayout.BUTTON_X, access().getX());
    assertEquals(
        ORIGIN_Y + TerminalSettingsLayout.settingY(TerminalSetting.ACCESS.row()), access().getY());
    assertEquals(ORIGIN_X + TerminalSettingsLayout.BUTTON_X, trusted().getX());
    assertEquals(
        ORIGIN_Y + TerminalSettingsLayout.settingY(TerminalSetting.TRUSTED.row()),
        trusted().getY());
    assertEquals(TerminalSettingsLayout.BUTTON_WIDTH, access().getWidth());
  }

  @Test
  void theRadiusSteppersSitAtEitherEndOfTheRadiusRow() {
    final int radiusY = ORIGIN_Y + TerminalSettingsLayout.settingY(TerminalSetting.RADIUS.row());

    assertEquals(ORIGIN_X + TerminalSettingsButtons.RADIUS_DOWN_X, radiusDown().getX());
    assertEquals(ORIGIN_X + TerminalSettingsButtons.RADIUS_UP_X, radiusUp().getX());
    assertEquals(radiusY, radiusDown().getY());
    assertEquals(radiusY, radiusUp().getY());
    assertEquals(
        ORIGIN_X + TerminalSettingsLayout.BUTTON_X + TerminalSettingsLayout.BUTTON_WIDTH,
        radiusUp().getX() + radiusUp().getWidth());
    assertEquals("-", radiusDown().getMessage().getString());
    assertEquals("+", radiusUp().getMessage().getString());
  }

  @ParameterizedTest(name = "visible={0}")
  @CsvSource({"true", "false"})
  void everyButtonFollowsTheSectionsVisibility(boolean visible) {
    buttons.update(owned(16), visible);

    added.forEach(button -> assertEquals(visible, button.visible));
  }

  @Test
  void nothingCanBeChangedBeforeTheServerAnswers() {
    buttons.update(NetworkSettingsView.pending(), true);

    added.forEach(button -> assertFalse(button.active));
    assertEquals("screen.total-storage.terminal.settings.pending", keyOf(access()));
    assertEquals("screen.total-storage.terminal.settings.pending", keyOf(trusted()));
  }

  @Test
  void aPlayerWhoMayNotAdministerCannotChangeAnything() {
    buttons.update(view(16, TerminalAccessMode.OPEN, false, List.of()), true);

    added.forEach(button -> assertFalse(button.active));
  }

  @ParameterizedTest(name = "radius {0}")
  @CsvSource({"1, false, true", "16, true, true", "64, true, false"})
  void theSteppersStopAtTheEndsOfTheRadiusRange(int radius, boolean down, boolean up) {
    buttons.update(owned(radius), true);

    assertTrue(access().active);
    assertTrue(trusted().active);
    assertEquals(down, radiusDown().active);
    assertEquals(up, radiusUp().active);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(TerminalAccessMode.class)
  void theAccessButtonShowsTheCurrentMode(TerminalAccessMode mode) {
    buttons.update(view(16, mode, true, List.of()), true);

    assertEquals(mode.displayName(), access().getMessage());
  }

  @Test
  void theTrustedButtonShowsWhoIsTrusted() {
    buttons.update(view(16, TerminalAccessMode.TRUSTED, true, List.of("Alex", "Steve")), true);

    assertEquals("Alex, Steve", trusted().getMessage().getString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(TerminalAccessMode.class)
  void pressingAccessAsksForTheNextMode(TerminalAccessMode mode) {
    buttons.update(view(16, mode, true, List.of()), true);

    access().onClick(0, 0);

    verify(edits).access(AccessModeCycle.next(mode));
  }

  @Test
  void pressingTrustedAsksToEditTheTrustedList() {
    buttons.update(owned(16), true);

    trusted().onClick(0, 0);

    verify(edits).trusted();
  }

  @Test
  void theSteppersAskForOneBlockLessOrMoreThanTheShownRadius() {
    buttons.update(owned(16), true);

    radiusDown().onClick(0, 0);
    radiusUp().onClick(0, 0);

    verify(edits).radius(15);
    verify(edits).radius(17);
  }
}
