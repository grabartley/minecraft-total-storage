package com.grahambartley.totalstorage.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class NextTick {
  private static final NextTickQueue QUEUE = new NextTickQueue();

  private NextTick() {}

  public static NextTickQueue queue() {
    return QUEUE;
  }

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> QUEUE.runPending());
  }
}
