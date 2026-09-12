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

public final class NetworkJobs {
  public static final int PROGRESS_INTERVAL_TICKS = 20;

  private final Map<TerminalAddress, Running> running = new LinkedHashMap<>();

  public boolean isRunning(TerminalAddress address) {
    return address != null && running.containsKey(address);
  }

  public int runningCount() {
    return running.size();
  }

  public boolean start(TerminalAddress address, NetworkJob job) {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(job, "job");
    if (running.containsKey(address)) {
      return false;
    }
    running.put(address, new Running(job));
    return true;
  }

  public boolean cancel(MinecraftServer server, TerminalAddress address) {
    final Running stopped = address == null ? null : running.remove(address);
    if (stopped == null) {
      return false;
    }
    stopped.job.cancel();
    tell(
        server,
        stopped.job,
        NetworkOperationMessages.cancelled(stopped.job.operation(), stopped.job.report()));
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
      if (next.job.advance(budget).isRunning()) {
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

    private Running(NetworkJob job) {
      this.job = job;
    }
  }
}
