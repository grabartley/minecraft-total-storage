package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
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

  public static final int PROGRESS_INTERVAL_TICKS = 5;

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
    NetworkOperationStatus.idle(server, address, stopped.job.operation());
    final Text message =
        NetworkOperationMessages.cancelled(stopped.job.operation(), stopped.job.report());
    final ServerPlayerEntity actor = actorOf(server, stopped.job);
    if (asking == null || asking.getUuid().equals(stopped.job.actor())) {
      TerminalNotice.done(actor, message);
      return true;
    }
    TerminalNotice.partial(actor, message);
    TerminalNotice.done(asking, message);
    return true;
  }

  public void tick(MinecraftServer server) {
    if (server == null || running.isEmpty()) {
      return;
    }
    final int budget = ServerConfigService.current().movesPerTickBudget();
    final Iterator<Map.Entry<TerminalAddress, Running>> jobs = running.entrySet().iterator();
    while (jobs.hasNext()) {
      final Map.Entry<TerminalAddress, Running> entry = jobs.next();
      final Running next = entry.getValue();
      if (advanced(next.job, budget).isRunning()) {
        reportProgress(server, entry.getKey(), next);
        continue;
      }
      jobs.remove();
      finish(server, entry.getKey(), next.job);
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

  private void reportProgress(MinecraftServer server, TerminalAddress address, Running next) {
    if (server.getTicks() - next.lastReportedTick < PROGRESS_INTERVAL_TICKS) {
      return;
    }
    next.lastReportedTick = server.getTicks();
    NetworkOperationStatus.running(
        server, address, next.job.operation(), next.job.report().percentDone());
  }

  private static void finish(MinecraftServer server, TerminalAddress address, NetworkJob job) {
    NetworkOperationStatus.idle(server, address, job.operation());
    final NetworkJobOutcome outcome = NetworkJobOutcome.of(job.operation(), job.report());
    TerminalNotice.report(
        actorOf(server, job), outcome.tone(), outcome.headline(), outcome.message());
  }

  private static ServerPlayerEntity actorOf(MinecraftServer server, NetworkJob job) {
    return server == null ? null : server.getPlayerManager().getPlayer(job.actor());
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
