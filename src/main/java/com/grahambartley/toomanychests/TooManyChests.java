package com.grahambartley.toomanychests;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManyChests implements ModInitializer {
  public static final String MOD_ID = "too-many-chests";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LOGGER.info("Too Many Chests initialized");
  }
}
