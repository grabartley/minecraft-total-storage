package com.grahambartley.toomanychests.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * The one queue of deferred work, and the tick that drains it.
 *
 * <p>Drained at the end of a tick rather than the start, because by then every world has finished
 * ticking and no chunk is part way through loading.
 */
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
