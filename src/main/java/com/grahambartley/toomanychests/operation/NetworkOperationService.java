package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.access.TerminalAdmin;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkOperationService {
  private final NetworkJobs jobs;
  private final OperationConfirmations confirmations;

  public NetworkOperationService() {
    this(NetworkOperations.jobs(), NetworkOperations.confirmations());
  }

  public NetworkOperationService(NetworkJobs jobs, OperationConfirmations confirmations) {
    this.jobs = Objects.requireNonNull(jobs, "jobs");
    this.confirmations = Objects.requireNonNull(confirmations, "confirmations");
  }

  public OperationStart sortNetwork(ServerPlayerEntity player, int syncId) {
    return start(player, syncId, ExpensiveOperation.SORT_NETWORK, NetworkSortJob::new);
  }

  public OperationStart consolidate(ServerPlayerEntity player, int syncId) {
    return start(player, syncId, ExpensiveOperation.CONSOLIDATE, ConsolidationJob::new);
  }

  private OperationStart start(
      ServerPlayerEntity player,
      int syncId,
      ExpensiveOperation operation,
      Function<NetworkOperationContext, NetworkJob> jobFor) {
    final TerminalOperationContext open = TerminalOperationGate.open(player, syncId).orElse(null);
    if (open == null) {
      return OperationStart.REFUSED;
    }
    if (!TerminalAdmin.mayAdminister(open.world(), open.terminal(), player)) {
      player.sendMessage(TerminalMessages.notYours(), true);
      return OperationStart.NOT_ALLOWED;
    }
    final NetworkOperationContext context = NetworkOperationContext.of(open);
    final TerminalAddress address = context.address();
    if (jobs.isRunning(address)) {
      jobs.cancel(open.world().getServer(), address, player);
      return OperationStart.CANCELLED;
    }
    final NetworkJob job = jobFor.apply(context);
    if (job.hasNothingToDo()) {
      player.sendMessage(NetworkOperationMessages.nothingToDo(operation), true);
      return OperationStart.NOTHING_TO_DO;
    }
    if (needsConfirming(player, context, address, operation)) {
      return OperationStart.NEEDS_CONFIRMATION;
    }
    if (!OperationGuards.allows(player, operation)) {
      return OperationStart.RATE_LIMITED;
    }
    jobs.start(address, job, context.server().getTicks());
    player.sendMessage(NetworkOperationMessages.started(operation), true);
    return OperationStart.STARTED;
  }

  private boolean needsConfirming(
      ServerPlayerEntity player,
      NetworkOperationContext context,
      TerminalAddress address,
      ExpensiveOperation operation) {
    final int containers = context.containers().size();
    final int threshold = ServerConfigService.current().largeOperationContainers();
    if (threshold <= 0 || containers < threshold) {
      return false;
    }
    if (confirmations.claim(player.getUuid(), address, operation, context.server().getTicks())) {
      return false;
    }
    player.sendMessage(NetworkOperationMessages.confirm(operation, containers), true);
    return true;
  }
}
