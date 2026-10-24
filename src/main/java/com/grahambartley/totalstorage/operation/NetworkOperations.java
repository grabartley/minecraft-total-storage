package com.grahambartley.totalstorage.operation;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class NetworkOperations {
  private static final NetworkJobs JOBS = new NetworkJobs();
  private static final OperationConfirmations CONFIRMATIONS = new OperationConfirmations();

  private NetworkOperations() {}

  public static NetworkJobs jobs() {
    return JOBS;
  }

  public static OperationConfirmations confirmations() {
    return CONFIRMATIONS;
  }

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(JOBS::tick);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> CONFIRMATIONS.forget(handler.getPlayer().getUuid()));
  }
}
