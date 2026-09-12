package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.terminal.TerminalAddress;
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

    assertTrue(jobs.start(TERMINAL, new RecordingJob()));
    assertTrue(jobs.isRunning(TERMINAL));
    assertEquals(1, jobs.runningCount());
  }

  @Test
  void oneNetworkRunsOneJobAtATime() {
    NetworkJobs jobs = new NetworkJobs();
    jobs.start(TERMINAL, new RecordingJob());

    assertFalse(jobs.start(TERMINAL, new RecordingJob()));
    assertEquals(1, jobs.runningCount());
  }

  @Test
  void twoNetworksRunTheirOwnJobs() {
    NetworkJobs jobs = new NetworkJobs();

    jobs.start(TERMINAL, new RecordingJob());
    jobs.start(ANOTHER_TERMINAL, new RecordingJob());

    assertEquals(2, jobs.runningCount());
  }

  @Test
  void cancellingStopsTheJobAndForgetsIt() {
    NetworkJobs jobs = new NetworkJobs();
    RecordingJob job = new RecordingJob();
    jobs.start(TERMINAL, job);

    assertTrue(jobs.cancel(null, TERMINAL));
    assertTrue(job.cancelled);
    assertFalse(jobs.isRunning(TERMINAL));
  }

  @Test
  void cancellingANetworkWithNoJobChangesNothing() {
    assertFalse(new NetworkJobs().cancel(null, TERMINAL));
  }

  @Test
  void cancellingNothingAtAllIsHarmless() {
    assertFalse(new NetworkJobs().cancel(null, null));
  }

  @Test
  void aNetworkWithNoAddressIsNeverRunning() {
    assertFalse(new NetworkJobs().isRunning(null));
  }

  @Test
  void clearingStopsEveryJob() {
    NetworkJobs jobs = new NetworkJobs();
    RecordingJob job = new RecordingJob();
    jobs.start(TERMINAL, job);

    jobs.clear();

    assertTrue(job.cancelled);
    assertEquals(0, jobs.runningCount());
  }

  @Test
  void aJobNeedsANetworkToRunOn() {
    NetworkJobs jobs = new NetworkJobs();

    assertThrows(NullPointerException.class, () -> jobs.start(null, new RecordingJob()));
    assertThrows(NullPointerException.class, () -> jobs.start(TERMINAL, null));
  }

  @Test
  void tickingWithoutAServerLeavesEveryJobWhereItWas() {
    NetworkJobs jobs = new NetworkJobs();
    jobs.start(TERMINAL, new RecordingJob());

    jobs.tick(null);

    assertTrue(jobs.isRunning(TERMINAL));
  }
}
