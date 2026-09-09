package com.grahambartley.toomanychests.transaction;

import com.grahambartley.toomanychests.lifecycle.ServerThread;

public final class NetworkTransactions {
  private static final TransactionRunner RUNNER = new TransactionRunner(ServerThread::isCurrent);

  private NetworkTransactions() {}

  public static TransactionRunner runner() {
    return RUNNER;
  }
}
