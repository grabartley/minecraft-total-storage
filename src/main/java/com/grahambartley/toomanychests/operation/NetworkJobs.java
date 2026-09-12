package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkJobs {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkJobs.class);

  public static final int PROGRESS_INTERVAL_TICKS = 20;

  private final Map<TerminalAddress, Running> running = new LinkedHashMap<>();

  public boolean isRunning(TerminalAddress address) {
    return address != null && running.containsKey(address);
  }

  public boolean start(TerminalAddress address, NetworkJob job, int startedAt) {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(job, "job");
    if (running.containsKey(address)) {
      return false;
    }
    running.put(address, new Running(job, startedAt));
    return true;
  }

  public boolean cancel(
      MinecraftServer server, TerminalAddress address, ServerPlayerEntity asking) {
    final Running stopped = address == null ? null : running.remove(address);
    if (stopped == null) {
      return false;
    }
    stopped.job.cancel();
    final Text message =
        NetworkOperationMessages.cancelled(stopped.job.operation(), stopped.job.report());
    tell(server, stopped.job, message);
    if (asking != null && !asking.getUuid().equals(stopped.job.actor())) {
      asking.sendMessage(message, true);
    }
    return true;
  }

  public void tick(MinecraftServer server) {
    if (server == null || running.isEmpty()) {
      return;
    }
    final int budget = ServerConfigService.current().movesPerTickBudget();
    final Iterator<Map.Entry<TerminalAddress, Running>> jobs = running.entrySet().iterator();
    while (jobs.hasNext()) {
      final Running next = jobs.next().getValue();
      if (advanced(next.job, budget).isRunning()) {
        reportProgress(server, next);
        continue;
      }
      jobs.remove();
      finish(server, next.job);
    }
  }

  public void clear() {
    running.values().forEach(next -> next.job.cancel());
    running.clear();
  }

  private static NetworkJobState advanced(NetworkJob job, int budget) {
    try {
      return job.advance(budget);
    } catch (RuntimeException failure) {
      LOGGER.error("Stopping a network operation that threw", failure);
      job.cancel();
      return NetworkJobState.DONE;
    }
  }

  private void reportProgress(MinecraftServer server, Running next) {
    if (server.getTicks() - next.lastReportedTick < PROGRESS_INTERVAL_TICKS) {
      return;
    }
    next.lastReportedTick = server.getTicks();
    tell(
        server,
        next.job,
        NetworkOperationMessages.progress(next.job.operation(), next.job.report()));
  }

  private static void finish(MinecraftServer server, NetworkJob job) {
    final OperationReport report = job.report();
    tell(
        server,
        job,
        report.cancelled()
            ? NetworkOperationMessages.cancelled(job.operation(), report)
            : NetworkOperationMessages.finished(job.operation(), report));
  }

  private static void tell(MinecraftServer server, NetworkJob job, Text message) {
    if (server == null) {
      return;
    }
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(job.actor());
    if (player != null) {
      player.sendMessage(message, true);
    }
  }

  private static final class Running {
    private final NetworkJob job;

    private int lastReportedTick;

    private Running(NetworkJob job, int startedAt) {
      this.job = job;
      this.lastReportedTick = startedAt;
    }
  }
}
