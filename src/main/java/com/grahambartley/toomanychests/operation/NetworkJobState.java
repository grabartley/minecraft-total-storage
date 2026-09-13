package com.grahambartley.toomanychests.operation;

public enum NetworkJobState {
  RUNNING,

  DONE;

  public boolean isRunning() {
    return this == RUNNING;
  }
}
