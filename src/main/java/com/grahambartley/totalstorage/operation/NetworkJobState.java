package com.grahambartley.totalstorage.operation;

public enum NetworkJobState {
  RUNNING,

  DONE;

  public boolean isRunning() {
    return this == RUNNING;
  }
}
