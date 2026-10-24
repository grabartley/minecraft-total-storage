package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class NetworkJobsTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("overworld"));

  private static final TerminalAddress TERMINAL =
      new TerminalAddress(OVERWORLD, new BlockPos(1, 2, 3));
  private static final TerminalAddress ANOTHER_TERMINAL =
      new TerminalAddress(OVERWORLD, new BlockPos(4, 2, 3));

  private static final class RecordingJob implements NetworkJob {
    private boolean cancelled;

    @Override
    public ExpensiveOperation operation() {
      return ExpensiveOperation.SORT_NETWORK;
    }

    @Override
    public UUID actor() {
      return UUID.nameUUIDFromBytes("actor".getBytes());
    }

    @Override
    public boolean hasNothingToDo() {
      return false;
    }

    @Override
    public NetworkJobState advance(int budget) {
      return NetworkJobState.RUNNING;
    }

    @Override
    public OperationReport report() {
      return OperationReport.nothing();
    }

    @Override
    public void cancel() {
      cancelled = true;
    }
  }

  @Test
  void aNetworkWithNoJobIsNotRunningOne() {
    assertFalse(new NetworkJobs().isRunning(TERMINAL));
  }

  @Test
  void startingAJobPutsTheNetworkToWork() {
    NetworkJobs jobs = new NetworkJobs();

    assertTrue(jobs.start(TERMINAL, new RecordingJob(), 0));
    assertTrue(jobs.isRunning(TERMINAL));
  }

  @Test
  void oneNetworkRunsOneJobAtATime() {
    NetworkJobs jobs = new NetworkJobs();
    jobs.start(TERMINAL, new RecordingJob(), 0);

    assertFalse(jobs.start(TERMINAL, new RecordingJob(), 0));
  }

  @Test
  void twoNetworksRunTheirOwnJobs() {
    NetworkJobs jobs = new NetworkJobs();

    jobs.start(TERMINAL, new RecordingJob(), 0);
    jobs.start(ANOTHER_TERMINAL, new RecordingJob(), 0);

    assertTrue(jobs.isRunning(TERMINAL));
    assertTrue(jobs.isRunning(ANOTHER_TERMINAL));
  }

  @Test
  void cancellingStopsTheJobAndForgetsIt() {
    NetworkJobs jobs = new NetworkJobs();
    RecordingJob job = new RecordingJob();
    jobs.start(TERMINAL, job, 0);

    assertTrue(jobs.cancel(null, TERMINAL, null));
    assertTrue(job.cancelled);
    assertFalse(jobs.isRunning(TERMINAL));
  }

  @Test
  void cancellingANetworkWithNoJobChangesNothing() {
    assertFalse(new NetworkJobs().cancel(null, TERMINAL, null));
  }

  @Test
  void cancellingNothingAtAllIsHarmless() {
    assertFalse(new NetworkJobs().cancel(null, null, null));
  }

  @Test
  void aNetworkWithNoAddressIsNeverRunning() {
    assertFalse(new NetworkJobs().isRunning(null));
  }

  @Test
  void clearingStopsEveryJob() {
    NetworkJobs jobs = new NetworkJobs();
    RecordingJob job = new RecordingJob();
    jobs.start(TERMINAL, job, 0);

    jobs.clear();

    assertTrue(job.cancelled);
    assertFalse(jobs.isRunning(TERMINAL));
  }

  @Test
  void aJobNeedsANetworkToRunOn() {
    NetworkJobs jobs = new NetworkJobs();

    assertThrows(NullPointerException.class, () -> jobs.start(null, new RecordingJob(), 0));
    assertThrows(NullPointerException.class, () -> jobs.start(TERMINAL, null, 0));
  }

  @Test
  void tickingWithoutAServerLeavesEveryJobWhereItWas() {
    NetworkJobs jobs = new NetworkJobs();
    jobs.start(TERMINAL, new RecordingJob(), 0);

    jobs.tick(null);

    assertTrue(jobs.isRunning(TERMINAL));
  }
}
