package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkJob;
import com.grahambartley.totalstorage.operation.NetworkJobState;
import com.grahambartley.totalstorage.operation.NetworkOperations;
import com.grahambartley.totalstorage.operation.OperationReport;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class NetworkJobsGameTest implements FabricGameTest {
  private static final String BATCH = "network-jobs";

  private static final int TICK_LIMIT = 40;
  private static final int TICKS_TO_SETTLE = 3;

  @AfterBatch(batchId = BATCH)
  public void clearJobs(ServerWorld world) {
    NetworkOperations.jobs().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStepThatThrowsStopsItsOwnOperationRatherThanTheServerTick(TestContext context) {
    final TerminalAddress address =
        TerminalAddress.of(
            context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
    final ThrowingJob job = new ThrowingJob();
    NetworkOperations.jobs().start(address, job, context.getWorld().getServer().getTicks());

    context.waitAndRun(
        TICKS_TO_SETTLE,
        () -> {
          context.assertFalse(
              NetworkOperations.jobs().isRunning(address),
              "A job whose step threw should have been dropped");
          context.assertTrue(job.cancelled, "A job whose step threw should have been cancelled");
          context.complete();
        });
  }

  private static final class ThrowingJob implements NetworkJob {
    private boolean cancelled;

    @Override
    public ExpensiveOperation operation() {
      return ExpensiveOperation.SORT_NETWORK;
    }

    @Override
    public UUID actor() {
      return UUID.nameUUIDFromBytes("nobody".getBytes());
    }

    @Override
    public boolean hasNothingToDo() {
      return false;
    }

    @Override
    public NetworkJobState advance(int budget) {
      throw new IllegalStateException("a step that could not settle");
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
}
