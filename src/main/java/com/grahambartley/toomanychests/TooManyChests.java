package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManyChests implements ModInitializer {
  public static final String MOD_ID = "too-many-chests";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LockedSlotsAttachment.register();
    ServerLifecycleEvents.SERVER_STARTING.register(ServerConfigService::loadFromWorld);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> ContainerMutations.registry().clear());
    LOGGER.info("Too Many Chests initialized");
  }
}
