package com.grahambartley.toomanychests.operation;

import java.util.UUID;

public interface NetworkJob {
  ExpensiveOperation operation();

  UUID actor();

  NetworkJobState advance(int budget);

  OperationReport report();

  void cancel();
}
