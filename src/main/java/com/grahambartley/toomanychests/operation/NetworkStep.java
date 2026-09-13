package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.transaction.NetworkOperation;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

public final class NetworkStep {
  private NetworkStep() {}

  public static <T> OperationResult<T> run(
      MinecraftServer server,
      Collection<StorageOrigin> watched,
      UUID actor,
      NetworkOperation<T> operation) {
    Objects.requireNonNull(operation, "operation");
    final OperationResult<T> result;
    final List<StorageOrigin> changed;
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(ContainerMutations.registry(), watched)) {
      result = NetworkTransactions.runner().run(operation);
      changed = List.copyOf(witness.changed());
    }
    NetworkResync.after(server, changed, actor);
    return result;
  }
}
