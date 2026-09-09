package com.grahambartley.toomanychests.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NextTickQueueTest {
  private static final String KEY = "terminal";
  private static final String OTHER_KEY = "chunk";

  private final NextTickQueue queue = new NextTickQueue();
  private final List<String> ran = new ArrayList<>();

  @Test
  void aQueueNobodyHasSubmittedToOwesNothing() {
    assertTrue(queue.isEmpty());
    assertEquals(0, queue.pendingCount());
    assertFalse(queue.isPending(KEY));
  }

  @Test
  void drainingAnEmptyQueueDoesNothingAtAll() {
    queue.runPending();

    assertTrue(queue.isEmpty());
    assertTrue(ran.isEmpty());
  }

  @Test
  void submittedWorkDoesNotRunUntilTheQueueIsDrained() {
    queue.submit(KEY, record("work"));

    assertTrue(ran.isEmpty());
    assertTrue(queue.isPending(KEY));

    queue.runPending();

    assertEquals(List.of("work"), ran);
  }

  @Test
  void drainingLeavesTheQueueOwingNothing() {
    queue.submit(KEY, record("work"));

    queue.runPending();

    assertTrue(queue.isEmpty());
    assertFalse(queue.isPending(KEY));
  }

  @DisplayName("work owed for one key is done once however many times it is asked for")
  @ParameterizedTest(name = "{0} submissions")
  @ValueSource(ints = {2, 3, 10})
  void collapsesRepeatedSubmissionsForOneKey(int submissions) {
    for (int submission = 0; submission < submissions; submission++) {
      queue.submit(KEY, record("work"));
    }

    assertEquals(1, queue.pendingCount());

    queue.runPending();

    assertEquals(List.of("work"), ran);
  }

  @Test
  void theLatestSubmissionForAKeyIsTheOneThatRuns() {
    queue.submit(KEY, record("first"));
    queue.submit(KEY, record("second"));

    queue.runPending();

    assertEquals(List.of("second"), ran);
  }

  @Test
  void resubmittingAKeyLeavesItWhereItAlreadyWasInTheOrder() {
    queue.submit("a", record("a"));
    queue.submit("b", record("b"));
    queue.submit("a", record("a again"));

    queue.runPending();

    assertEquals(List.of("a again", "b"), ran);
  }

  @Test
  void distinctKeysAreDistinctPiecesOfWork() {
    queue.submit(KEY, record("terminal"));
    queue.submit(OTHER_KEY, record("chunk"));

    assertEquals(2, queue.pendingCount());

    queue.runPending();

    assertEquals(List.of("terminal", "chunk"), ran);
  }

  @Test
  void workRunsInTheOrderItWasAskedFor() {
    queue.submit("a", record("a"));
    queue.submit("b", record("b"));
    queue.submit("c", record("c"));

    queue.runPending();

    assertEquals(List.of("a", "b", "c"), ran);
  }

  @Test
  void cancelledWorkIsNeverRun() {
    queue.submit(KEY, record("work"));

    queue.cancel(KEY);

    assertFalse(queue.isPending(KEY));
    queue.runPending();
    assertTrue(ran.isEmpty());
  }

  @Test
  void cancellingOneKeyLeavesTheRestOwed() {
    queue.submit(KEY, record("terminal"));
    queue.submit(OTHER_KEY, record("chunk"));

    queue.cancel(KEY);
    queue.runPending();

    assertEquals(List.of("chunk"), ran);
  }

  @Test
  void aKeyCancelledAndSubmittedAgainIsOwedAgain() {
    queue.submit(KEY, record("first"));
    queue.cancel(KEY);
    queue.submit(KEY, record("second"));

    queue.runPending();

    assertEquals(List.of("second"), ran);
  }

  @Test
  void cancellingWorkNobodyAskedForIsHarmless() {
    queue.cancel(KEY);

    assertTrue(queue.isEmpty());
  }

  @Test
  void clearingDropsEverythingOwedWithoutRunningIt() {
    queue.submit(KEY, record("terminal"));
    queue.submit(OTHER_KEY, record("chunk"));

    queue.clear();

    assertTrue(queue.isEmpty());
    queue.runPending();
    assertTrue(ran.isEmpty());
  }

  @Test
  void workSubmittedWhileDrainingWaitsForTheNextDrain() {
    queue.submit(
        KEY,
        () -> {
          ran.add("first");
          queue.submit(OTHER_KEY, record("second"));
        });

    queue.runPending();

    assertEquals(List.of("first"), ran);
    assertTrue(queue.isPending(OTHER_KEY));

    queue.runPending();

    assertEquals(List.of("first", "second"), ran);
  }

  @Test
  void workThatResubmitsItsOwnKeyIsOwedAgainRatherThanLooping() {
    queue.submit(
        KEY,
        () -> {
          ran.add("run");
          queue.submit(KEY, record("run"));
        });

    queue.runPending();

    assertEquals(List.of("run"), ran);
    assertEquals(1, queue.pendingCount());
  }

  @DisplayName("a submission missing either half of what it needs is ignored")
  @Test
  void ignoresASubmissionWithNoKeyOrNoWork() {
    queue.submit(null, record("no key"));
    queue.submit(KEY, null);

    assertTrue(queue.isEmpty());
    queue.runPending();
    assertTrue(ran.isEmpty());
  }

  @Test
  void nothingIsEverPendingForANullKey() {
    queue.submit(KEY, record("work"));

    assertFalse(queue.isPending(null));
  }

  private Runnable record(String name) {
    return () -> ran.add(name);
  }
}
