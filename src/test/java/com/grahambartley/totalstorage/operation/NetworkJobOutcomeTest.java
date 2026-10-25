package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NetworkJobOutcomeTest {
  private static final String PREFIX = "operation.total-storage.";

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static OperationReport report(int excluded, int restricted, boolean cancelled) {
    return new OperationReport(3, 3, 4, 2, 0L, excluded, restricted, cancelled);
  }

  @Test
  void aCleanRunIsDoneAndLeadsWithItsSummary() {
    final NetworkJobOutcome outcome =
        NetworkJobOutcome.of(ExpensiveOperation.SORT_NETWORK, report(0, 0, false));

    assertEquals(TerminalNoticeTone.DONE, outcome.tone());
    assertEquals(outcome.message(), outcome.headline());
    assertEquals(PREFIX + "sort.network.done", keyOf(outcome.message()));
  }

  @ParameterizedTest
  @CsvSource({"1, 0", "0, 2", "3, 1"})
  void aRunThatSkippedContainersIsPartialAndSaysSoUpFront(int excluded, int restricted) {
    final NetworkJobOutcome outcome =
        NetworkJobOutcome.of(ExpensiveOperation.CONSOLIDATE, report(excluded, restricted, false));

    assertEquals(TerminalNoticeTone.PARTIAL, outcome.tone());
    assertEquals(PREFIX + "skipped.headline", keyOf(outcome.headline()));
    assertNotEquals(outcome.headline(), outcome.message());
  }

  @Test
  void aStoppedRunIsPartialWhateverItSkipped() {
    final NetworkJobOutcome outcome =
        NetworkJobOutcome.of(ExpensiveOperation.SORT_NETWORK, report(2, 0, true));

    assertEquals(TerminalNoticeTone.PARTIAL, outcome.tone());
    assertEquals(PREFIX + "sort.network.cancelled", keyOf(outcome.headline()));
  }
}
