package com.grahambartley.toomanychests.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NextTickQueue {
  private final Map<Object, Runnable> pending = new LinkedHashMap<>();

  public void submit(Object key, Runnable work) {
    if (key == null || work == null) {
      return;
    }
    pending.put(key, work);
  }

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

  public void runPending() {
    if (pending.isEmpty()) {
      return;
    }
    final List<Runnable> due = new ArrayList<>(pending.values());
    pending.clear();
    due.forEach(Runnable::run);
  }
}
