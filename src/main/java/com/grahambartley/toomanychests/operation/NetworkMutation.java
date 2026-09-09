package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.transaction.NetworkOperation;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.List;
import java.util.Objects;

public final class NetworkMutation {
  private NetworkMutation() {}

  public static <T> OperationResult<T> run(
      TerminalOperationContext context, NetworkOperation<T> operation) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(operation, "operation");
    final OperationResult<T> result;
    final List<StorageOrigin> changed;
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(
            ContainerMutations.registry(), context.network().containers())) {
      result = NetworkTransactions.runner().run(operation);
      changed = List.copyOf(witness.changed());
    }
    NetworkResync.after(context.world().getServer(), changed, context.player().getUuid());
    return result;
  }
}
