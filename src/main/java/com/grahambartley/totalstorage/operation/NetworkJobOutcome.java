package com.grahambartley.totalstorage.operation;

import java.util.Objects;
import net.minecraft.text.Text;

public record NetworkJobOutcome(TerminalNoticeTone tone, Text headline, Text message) {
  public NetworkJobOutcome {
    Objects.requireNonNull(tone, "tone");
    Objects.requireNonNull(message, "message");
    headline = headline == null ? message : headline;
  }

  public static NetworkJobOutcome of(ExpensiveOperation operation, OperationReport report) {
    if (report.cancelled()) {
      final Text stopped = NetworkOperationMessages.cancelled(operation, report);
      return new NetworkJobOutcome(TerminalNoticeTone.PARTIAL, stopped, stopped);
    }
    final Text summary = NetworkOperationMessages.finished(operation, report);
    if (report.skippedAnything()) {
      return new NetworkJobOutcome(
          TerminalNoticeTone.PARTIAL, NetworkOperationMessages.skipped(report), summary);
    }
    return new NetworkJobOutcome(TerminalNoticeTone.DONE, summary, summary);
  }
}
