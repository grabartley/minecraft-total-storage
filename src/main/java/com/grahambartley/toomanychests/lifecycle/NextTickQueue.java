package com.grahambartley.toomanychests.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Work the world has asked for that must not run where it was asked for.
 *
 * <p>Chunk loading is the reason this exists. Fabric delivers block entity and chunk load events
 * from inside the chunk load itself, and anything that asks the chunk manager a question from there
 * re-enters the main thread's task pump and never comes back, which is a world that sits on
 * "Preparing spawn area" forever. Remembering the event and doing the work on the next tick costs
 * one tick and makes that whole class of hang impossible.
 *
 * <p>Keyed rather than a plain list, because the same terminal or the same chunk can be announced
 * more than once before the tick arrives and the work is owed once. The key names the work, not the
 * caller.
 *
 * <p>Server side and single threaded, like the world events that feed it.
 */
public final class NextTickQueue {
  private final Map<Object, Runnable> pending = new LinkedHashMap<>();

  /**
   * The latest submission for a key wins, and the key keeps the place it first took. Later
   * submissions describe the same work against something newer, such as the block entity a position
   * holds now, so the newest description is the one worth running.
   */
  public void submit(Object key, Runnable work) {
    if (key == null || work == null) {
      return;
    }
    pending.put(key, work);
  }

  /** Withdraws work whose reason has gone, so a terminal that unloads again is never woken. */
  public void cancel(Object key) {
    if (key != null) {
      pending.remove(key);
    }
  }

  public boolean isPending(Object key) {
    return key != null && pending.containsKey(key);
  }

  public int pendingCount() {
    return pending.size();
  }

  public boolean isEmpty() {
    return pending.isEmpty();
  }

  public void clear() {
    pending.clear();
  }

  /**
   * Runs everything owed, in the order it was asked for. The queue is emptied before any of it
   * runs, so work that submits more work is owed it on the next tick rather than extending this
   * one.
   */
  public void runPending() {
    if (pending.isEmpty()) {
      return;
    }
    final List<Runnable> due = new ArrayList<>(pending.values());
    pending.clear();
    due.forEach(Runnable::run);
  }
}
