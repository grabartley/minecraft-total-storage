package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.grahambartley.totalstorage.screen.TerminalScreen;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LockCuesTest {
  static Stream<Arguments> screens() {
    return Stream.of(
        Arguments.of("the terminal", mock(TerminalScreen.class), true),
        Arguments.of("the player inventory", mock(InventoryScreen.class), false),
        Arguments.of("a furnace", mock(FurnaceScreen.class), false),
        Arguments.of("a chest", mock(GenericContainerScreen.class), false),
        Arguments.of("a shulker box", mock(ShulkerBoxScreen.class), false),
        Arguments.of("no screen", null, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("screens")
  void theBadgeAndTintOnlyBelongOnTheTerminal(String name, Screen screen, boolean shown) {
    assertEquals(shown, LockCues.belongOn(screen));
  }
}
