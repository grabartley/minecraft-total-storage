package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.text.CountedNoun;
import net.minecraft.text.Text;

public final class NetworkOperationMessages {
  private static final String PREFIX = "operation." + TooManyChests.MOD_ID + ".";

  private NetworkOperationMessages() {}

  public static Text started(ExpensiveOperation operation) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line("consolidate.started", "Gathering scattered items, click again to stop")
        : line("sort.network.started", "Sorting every container, click again to stop");
  }

  public static Text progress(ExpensiveOperation operation, OperationReport report) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line("consolidate.progress", "Consolidating, %s%% done", report.percentDone())
        : line("sort.network.progress", "Sorting, %s%% done", report.percentDone());
  }

  public static Text finished(ExpensiveOperation operation, OperationReport report) {
    final Text summary =
        operation == ExpensiveOperation.CONSOLIDATE ? gathered(report) : sorted(report);
    return report.skippedAnything() ? withSkipped(summary, report) : summary;
  }

  private static Text gathered(OperationReport report) {
    return report.containersSorted() == 0
        ? line(
            "consolidate.done.gathered",
            "Gathered %s into as few containers as they fit",
            CountedNoun.ITEMS.of(report.itemsMoved()))
        : line(
            "consolidate.done",
            "Gathered %s and sorted %s, merging %s",
            CountedNoun.ITEMS.of(report.itemsMoved()),
            CountedNoun.CONTAINERS.of(report.containersSorted()),
            CountedNoun.STACKS.of(report.stacksMerged()));
  }

  private static Text sorted(OperationReport report) {
    return report.containersSorted() == 0
        ? line("sort.network.done.tidy", "Every container was already sorted")
        : line(
            "sort.network.done",
            "Sorted %s, merging %s",
            CountedNoun.CONTAINERS.of(report.containersSorted()),
            CountedNoun.STACKS.of(report.stacksMerged()));
  }

  public static Text cancelled(ExpensiveOperation operation, OperationReport report) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line(
            "consolidate.cancelled",
            "Stopped consolidating after gathering %s",
            CountedNoun.ITEMS.of(report.itemsMoved()))
        : line(
            "sort.network.cancelled",
            "Stopped sorting after %s",
            CountedNoun.CONTAINERS.of(report.containersSorted()));
  }

  public static Text confirm(ExpensiveOperation operation, int containers) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line(
            "consolidate.confirm",
            "This network holds %s, click again to gather every scattered stack",
            CountedNoun.CONTAINERS.of(containers))
        : line(
            "sort.network.confirm",
            "This network holds %s, click again to sort every one",
            CountedNoun.CONTAINERS.of(containers));
  }

  public static Text nothingToDo(ExpensiveOperation operation) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line("consolidate.empty", "Nothing is scattered across more than one container")
        : line("sort.network.empty", "There is nothing here to sort");
  }

  public static Text quickStacked(long items, int containers) {
    return line(
        "quickstack.done",
        "Stacked %s into %s",
        CountedNoun.ITEMS.of(items),
        CountedNoun.NEARBY_CONTAINERS.of(containers));
  }

  public static Text nothingToQuickStack() {
    return line("quickstack.empty", "Nothing here belongs in a container nearby");
  }

  private static Text withSkipped(Text summary, OperationReport report) {
    if (report.restrictedSkipped() == 0) {
      return line(
          "skipped.excluded",
          "%s, skipping %s",
          summary,
          CountedNoun.EXCLUDED_CONTAINERS.of(report.excludedSkipped()));
    }
    if (report.excludedSkipped() == 0) {
      return line(
          "skipped.restricted",
          "%s, skipping %s this mod will not rearrange",
          summary,
          CountedNoun.CONTAINERS.of(report.restrictedSkipped()));
    }
    return line(
        "skipped.both",
        "%s, skipping %s and %s this mod will not rearrange",
        summary,
        CountedNoun.EXCLUDED_CONTAINERS.of(report.excludedSkipped()),
        CountedNoun.CONTAINERS.of(report.restrictedSkipped()));
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
