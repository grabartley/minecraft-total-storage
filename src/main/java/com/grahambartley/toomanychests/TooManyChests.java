package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.command.TooManyChestsCommand;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import com.grahambartley.toomanychests.lifecycle.ServerThread;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.network.PayloadCapLog;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.screenhandler.TerminalListingBroadcast;
import com.grahambartley.toomanychests.screenhandler.TerminalPanelRefresh;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLifecycle;
import com.grahambartley.toomanychests.terminal.ContainerEligibilityRefresh;
import com.grahambartley.toomanychests.terminal.TerminalLifecycle;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalRadiusClamp;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManyChests implements ModInitializer {
  public static final String MOD_ID = "too-many-chests";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    ModSounds.initialize();
    ModBlocks.initialize();
    ModItems.initialize();
    ModBlockEntities.initialize();
    ModScreenHandlers.initialize();
    LockedSlotsAttachment.register();
    ModNetworking.register();
    TooManyChestsCommand.register();
    ServerThread.register();
    NextTick.register();
    ContainerLifecycle.register();
    TerminalLifecycle.register();
    TerminalAccessControl.register();
    OperationGuards.register();
    TerminalScreenLifecycle.register();
    new TerminalListingBroadcast(ModNetworking.listings()).register();
    TerminalRadiusClamp.register();
    ContainerEligibilityRefresh.register();
    TerminalPanelRefresh.register();
    ServerLifecycleEvents.SERVER_STARTING.register(
        server -> {
          ServerConfigService.loadFromWorld(server);
          ContainerEligibilityRefresh.seedFrom(ServerConfigService.current());
        });
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          NextTick.queue().clear();
          TerminalNetworks.power().shutDownAll();
          TerminalAccessControl.viewers().clear();
          OperationGuards.cooldowns().clear();
          PayloadCapLog.clear();
          ContainerMutations.registry().clear();
          ContainerLifecycle.registry().clear();
          ContainerEligibilityRefresh.seedFrom(TooManyChestsConfig.defaults());
        });
    LOGGER.info("Too Many Chests initialized");
  }
}
