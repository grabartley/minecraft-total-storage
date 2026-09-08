package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.RoleTally;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
        RoleTally.EMPTY,
        0,
        total,
        IntStream.range(0, Math.min(total, TerminalScreenLayout.CONTAINER_ROWS))
            .mapToObj(
                index ->
                    new ContainerRoleEntry(
                        new BlockPos(index, 64, 0), ContainerRole.GENERAL, 0, 0L, index))
            .toList());
  }

  static java.util.stream.Stream<Integer> everyRow() {
    return IntStream.range(0, TerminalScreenLayout.CONTAINER_ROWS).boxed();
  }

  @ParameterizedTest
  @MethodSource("everyRow")
  void aClickOnARowFindsThatRowWhileTheListIsAtTheTop(int row) {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertEquals(
        row, panel.rowAt(TerminalScreenLayout.SETTINGS_X, TerminalScreenLayout.containerRowY(row)));
  }

  /** Scrolled down, the same screen row is a different container, which is the whole hazard. */
  @Test
  void aClickOnARowFollowsWhereTheListIsScrolledTo() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(TerminalScreenLayout.CONTAINER_ROWS + 3));
    panel.scroll().scrollBy(2);

    assertEquals(
        2, panel.rowAt(TerminalScreenLayout.SETTINGS_X, TerminalScreenLayout.containerRowY(0)));
  }

  @Test
  void aClickOutsideTheListFindsNoRow() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();

    assertTrue(
        panel.rowAt(TerminalScreenLayout.SETTINGS_X, TerminalScreenLayout.CONTAINER_LIST_Y - 1)
            < 0);
    assertTrue(
        panel.rowAt(TerminalScreenLayout.SETTINGS_X - 1, TerminalScreenLayout.CONTAINER_LIST_Y)
            < 0);
  }

  /** A list that shrank under a scrolled panel must not leave it pointing past the end. */
  @Test
  void theScrollIsBroughtBackInsideAListThatShrank() {
    TerminalSettingsPanel panel = new TerminalSettingsPanel();
    panel.fit(viewOf(TerminalScreenLayout.CONTAINER_ROWS + 5));
    panel.scroll().scrollBy(5);

    panel.fit(viewOf(1));

    assertEquals(0, panel.scroll().firstIndex());
  }
}
