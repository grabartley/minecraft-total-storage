package com.grahambartley.totalstorage.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.screen.ConfigScreen;
import com.grahambartley.totalstorage.screen.ConfigScreenText;
import net.minecraft.client.gui.screen.Screen;
import org.junit.jupiter.api.Test;

class TotalStorageModMenuTest {
  @Test
  void modMenuIsHandedTheSettingsScreen() {
    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(null);

    assertNotNull(built);
    assertEquals(ConfigScreen.class, built.getClass());
  }

  @Test
  void theScreenWearsTheSettingsTitle() {
    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(null);

    assertEquals(ConfigScreenText.title().getString(), built.getTitle().getString());
  }

  @Test
  void theFactoryThreadsTheScreenThatOpenedItThroughAsTheParent() {
    final Screen opener = new ConfigScreen(null);

    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(opener);

    assertSame(opener, ((ConfigScreen) built).parent());
  }
}
