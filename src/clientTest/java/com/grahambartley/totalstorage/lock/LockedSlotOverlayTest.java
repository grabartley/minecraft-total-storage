package com.grahambartley.totalstorage.lock;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.grahambartley.totalstorage.config.ClientPreferences;
import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import java.nio.file.Path;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LockedSlotOverlayTest {
  private static final int LEFT = 44;
  private static final int TOP = 17;

  @TempDir Path folder;

  @BeforeEach
  void turnTheTintOn() {
    ClientPreferencesStore.save(
        folder.resolve("client.json"),
        ClientPreferences.defaults().withLockedSlotTintVisible(true));
  }

  @AfterEach
  void putTheDefaultsBack() {
    ClientPreferencesStore.save(folder.resolve("client.json"), ClientPreferences.defaults());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.totalstorage.lock.LockCuesTest#screens")
  void aLockedSlotIsTintedOnlyOnTheTerminal(String name, Screen screen, boolean terminal) {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotOverlay.draw(context, screen, true, LEFT, TOP);

    verify(context, times(terminal ? 1 : 0))
        .fill(LEFT, TOP, LEFT + 16, TOP + 16, LockedSlotTint.colour(true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.totalstorage.lock.LockCuesTest#screens")
  void anUnlockedSlotIsNeverTinted(String name, Screen screen, boolean terminal) {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotOverlay.draw(context, screen, false, LEFT, TOP);

    verify(context, never()).fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }
}
