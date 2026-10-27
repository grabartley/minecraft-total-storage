package com.grahambartley.totalstorage;

import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.command.TotalStorageCommand;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import com.grahambartley.totalstorage.lifecycle.ContainerLifecycle;
import com.grahambartley.totalstorage.lifecycle.NextTick;
import com.grahambartley.totalstorage.lifecycle.ServerThread;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import com.grahambartley.totalstorage.mutation.ContainerMutations;
import com.grahambartley.totalstorage.network.ClientModRequirement;
import com.grahambartley.totalstorage.network.ModNetworking;
import com.grahambartley.totalstorage.network.PayloadCapLog;
import com.grahambartley.totalstorage.operation.NetworkOperations;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.screenhandler.TerminalListingBroadcast;
import com.grahambartley.totalstorage.screenhandler.TerminalPanelRefresh;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLifecycle;
import com.grahambartley.totalstorage.terminal.ContainerEligibilityRefresh;
import com.grahambartley.totalstorage.terminal.TerminalCapacityBroadcast;
import com.grahambartley.totalstorage.terminal.TerminalLifecycle;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import com.grahambartley.totalstorage.terminal.TerminalRadiusClamp;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotalStorage implements ModInitializer {
  public static final String MOD_ID = "total-storage";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    ModSounds.initialize();
    ModBlocks.initialize();
    ModItems.initialize();
    ModBlockEntities.initialize();
    ModScreenHandlers.initialize();
    LockedSlotsAttachment.register();
    FuzzySearchAttachment.register();
    ModNetworking.register();
    ClientModRequirement.register();
    TotalStorageCommand.register();
    ServerThread.register();
    NextTick.register();
    ContainerLifecycle.register();
    TerminalLifecycle.register();
    TerminalAccessControl.register();
    OperationGuards.register();
    NetworkOperations.register();
    TerminalScreenLifecycle.register();
    TerminalListingBroadcast.register();
    new TerminalCapacityBroadcast().register();
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
          NetworkOperations.jobs().clear();
          NetworkOperations.confirmations().clear();
          PayloadCapLog.clear();
          ContainerMutations.registry().clear();
          ContainerLifecycle.registry().clear();
        });
    LOGGER.info("Total Storage initialized");
  }
}
