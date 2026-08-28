package com.grahambartley.toomanychests.transaction;

import com.grahambartley.toomanychests.lifecycle.ServerThread;

/** The one runner every mutating path in the mod uses. */
public final class NetworkTransactions {
  private static final TransactionRunner RUNNER = new TransactionRunner(ServerThread::isCurrent);

  private NetworkTransactions() {}

  public static TransactionRunner runner() {
    return RUNNER;
  }
}
