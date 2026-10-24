package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.mutation.ContainerMutationWitness;
import com.grahambartley.totalstorage.mutation.ContainerMutations;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.transaction.NetworkOperation;
import com.grahambartley.totalstorage.transaction.NetworkTransactions;
import com.grahambartley.totalstorage.transaction.OperationResult;
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
