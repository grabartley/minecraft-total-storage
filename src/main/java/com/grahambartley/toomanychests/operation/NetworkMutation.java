package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.transaction.NetworkOperation;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.Objects;

/**
 * Runs one operation against a network and tells everyone watching what it did.
 *
 * <p>The resync is bound to the commit rather than to the tick that follows it. A tick sweep can
 * only ask whether a network has moved since it last looked, which is enough to correct a listing
 * eventually and not enough to correct it before the next click; the click that lands in between is
 * one the server has to refuse and resync anyway, so the resync may as well happen when the move
 * does.
 *
 * <p>Nothing is resynced for an operation that rolled back, and this is not a special case handled
 * here. A container announces a change on commit and not before, so a rolled back operation is
 * simply witnessed as having touched nothing.
 */
public final class NetworkMutation {

  private NetworkMutation() {}

  public static <T> OperationResult<T> run(
      TerminalOperationContext context, NetworkOperation<T> operation) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(operation, "operation");
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.over(
            ContainerMutations.registry(), context.network().containers())) {
      final OperationResult<T> result = NetworkTransactions.runner().run(operation);
      NetworkResync.after(
          context.world().getServer(), witness.changed(), context.player().getUuid());
      return result;
    }
  }
}
