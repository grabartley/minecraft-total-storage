package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalIconButtonTest {
  private final List<String> pressed = new ArrayList<>();

  private TerminalIconButton button(int size) {
    return new TerminalIconButton(
        0,
        0,
        size,
        TerminalSprite.CROSS,
        TerminalSprite.CROSS_UNAVAILABLE,
        Text.literal("Clear"),
        () -> pressed.add("pressed"));
  }

  @ParameterizedTest
  @CsvSource({"true,CROSS", "false,CROSS_UNAVAILABLE"})
  void anAvailableButtonShowsItsIconAndAnUnavailableOneItsMutedIcon(
      boolean active, TerminalSprite expected) {
    final TerminalIconButton button = button(14);

    button.active = active;

    assertEquals(expected, button.shownIcon());
  }

  @ParameterizedTest
  @CsvSource({"14,3", "16,4", "8,0", "12,2"})
  void theIconIsCentredInTheButton(int size, int expectedOffset) {
    assertEquals(expectedOffset, TerminalIconButton.iconOffset(size));
  }

  @ParameterizedTest
  @ValueSource(ints = {8, 14, 20})
  void theIconAlwaysFitsInsideTheButton(int size) {
    final int offset = TerminalIconButton.iconOffset(size);

    assertTrue(offset >= 0);
    assertTrue(offset + TerminalIconButton.ICON_SIZE <= size);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3})
  void everyClickRunsTheAction(int clicks) {
    final TerminalIconButton button = button(14);

    for (int click = 0; click < clicks; click++) {
      button.onClick(0, 0);
    }

    assertEquals(clicks, pressed.size());
  }
}
