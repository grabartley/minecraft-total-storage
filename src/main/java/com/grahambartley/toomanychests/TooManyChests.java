package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.lifecycle.ServerThread;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.network.ModNetworking;
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
    ModNetworking.register();
    ServerThread.register();
    ContainerLifecycle.register();
    ServerLifecycleEvents.SERVER_STARTING.register(ServerConfigService::loadFromWorld);
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          ContainerMutations.registry().clear();
          ContainerLifecycle.registry().clear();
        });
    LOGGER.info("Too Many Chests initialized");
  }
}
