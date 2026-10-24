package com.grahambartley.totalstorage.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.screen.ServerConfigScreen;
import com.grahambartley.totalstorage.screen.ServerConfigText;
import net.minecraft.client.gui.screen.Screen;
import org.junit.jupiter.api.Test;

class TotalStorageModMenuTest {
  @Test
  void modMenuIsHandedTheServerSettingsScreen() {
    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(null);

    assertNotNull(built);
    assertEquals(ServerConfigScreen.class, built.getClass());
  }

  @Test
  void theScreenWearsTheServerSettingsTitle() {
    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(null);

    assertEquals(ServerConfigText.title().getString(), built.getTitle().getString());
  }

  @Test
  void theFactoryThreadsTheScreenThatOpenedItThroughAsTheParent() {
    final Screen opener = new ServerConfigScreen(null);

    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(opener);

    assertSame(opener, ((ServerConfigScreen) built).parent());
  }
}
