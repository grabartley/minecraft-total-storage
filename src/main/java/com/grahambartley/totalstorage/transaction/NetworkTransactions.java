package com.grahambartley.totalstorage.transaction;

import com.grahambartley.totalstorage.lifecycle.ServerThread;

public final class NetworkTransactions {
  private static final TransactionRunner RUNNER = new TransactionRunner(ServerThread::isCurrent);

  private NetworkTransactions() {}

  public static TransactionRunner runner() {
    return RUNNER;
  }
}
