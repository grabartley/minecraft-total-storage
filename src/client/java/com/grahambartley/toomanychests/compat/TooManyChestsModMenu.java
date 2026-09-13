package com.grahambartley.toomanychests.compat;

import com.grahambartley.toomanychests.screen.ServerConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class TooManyChestsModMenu implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return ServerConfigScreen::new;
  }
}
