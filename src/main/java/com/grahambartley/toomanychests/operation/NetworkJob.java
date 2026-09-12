package com.grahambartley.toomanychests.operation;

import java.util.UUID;

public interface NetworkJob {
  ExpensiveOperation operation();

  UUID actor();

  boolean hasNothingToDo();

  NetworkJobState advance(int budget);

  OperationReport report();

  void cancel();
}
