package com.grahambartley.totalstorage.compat;

import com.grahambartley.totalstorage.screen.ServerConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class TotalStorageModMenu implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return ServerConfigScreen::new;
  }
}
