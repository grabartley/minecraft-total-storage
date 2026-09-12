package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.transaction.NetworkOperation;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.Objects;

public final class NetworkMutation {
  private NetworkMutation() {}

  public static <T> OperationResult<T> run(
      TerminalOperationContext context, NetworkOperation<T> operation) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(operation, "operation");
    return NetworkStep.run(
        context.world().getServer(),
        context.network().containers(),
        context.player().getUuid(),
        operation);
  }
}
