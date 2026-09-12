package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.TooManyChests;
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
        operation == ExpensiveOperation.CONSOLIDATE
            ? line(
                "consolidate.done",
                "Gathered %s items and sorted %s containers, merging %s stacks",
                report.itemsMoved(),
                report.containersSorted(),
                report.stacksMerged())
            : line(
                "sort.network.done",
                "Sorted %s containers, merging %s stacks",
                report.containersSorted(),
                report.stacksMerged());
    return report.skippedAnything() ? withSkipped(summary, report) : summary;
  }

  public static Text cancelled(ExpensiveOperation operation, OperationReport report) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line(
            "consolidate.cancelled",
            "Stopped consolidating after gathering %s items",
            report.itemsMoved())
        : line(
            "sort.network.cancelled",
            "Stopped sorting after %s containers",
            report.containersSorted());
  }

  public static Text confirm(ExpensiveOperation operation, int containers) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line(
            "consolidate.confirm",
            "This network holds %s containers, click again to gather every scattered stack",
            containers)
        : line(
            "sort.network.confirm",
            "This network holds %s containers, click again to sort every one",
            containers);
  }

  public static Text nothingToDo(ExpensiveOperation operation) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? line("consolidate.empty", "Nothing is scattered across more than one container")
        : line("sort.network.empty", "There is nothing here to sort");
  }

  public static Text quickStacked(long items, int containers) {
    return line("quickstack.done", "Stacked %s items into %s nearby containers", items, containers);
  }

  public static Text nothingToQuickStack() {
    return line("quickstack.empty", "Nothing here belongs in a container nearby");
  }

  private static Text withSkipped(Text summary, OperationReport report) {
    if (report.restrictedSkipped() == 0) {
      return line(
          "skipped.excluded", "%s, skipping %s excluded", summary, report.excludedSkipped());
    }
    if (report.excludedSkipped() == 0) {
      return line(
          "skipped.restricted",
          "%s, skipping %s this mod will not rearrange",
          summary,
          report.restrictedSkipped());
    }
    return line(
        "skipped.both",
        "%s, skipping %s excluded and %s this mod will not rearrange",
        summary,
        report.excludedSkipped(),
        report.restrictedSkipped());
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
