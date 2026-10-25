package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NetworkOperationMessagesTest {
  private static final String PREFIX = "operation.total-storage.";
  private static final String COUNT_PREFIX = "count.total-storage.";

  private static TranslatableTextContent contentOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  private static String keyOfArgument(Text line, int index) {
    return contentOf((Text) contentOf(line).getArgs()[index]).getKey();
  }

  private static OperationReport sorted(int containers, int stacks) {
    return new OperationReport(1, 1, containers, stacks, 0L, 0, 0, false);
  }

  private static OperationReport gathered(long items, int containers, int stacks) {
    return new OperationReport(1, 1, containers, stacks, items, 0, 0, false);
  }

  @ParameterizedTest
  @CsvSource({"1, containers.one", "2, containers", "9, containers"})
  void aFinishedSortCountsItsContainersInTheRightNumber(int containers, String key) {
    Text line =
        NetworkOperationMessages.finished(ExpensiveOperation.SORT_NETWORK, sorted(containers, 3));

    assertEquals(PREFIX + "sort.network.done", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 0));
  }

  @ParameterizedTest
  @CsvSource({"1, stacks.one", "5, stacks", "0, stacks"})
  void aFinishedSortCountsTheStacksItMergedInTheRightNumber(int stacks, String key) {
    Text line =
        NetworkOperationMessages.finished(ExpensiveOperation.SORT_NETWORK, sorted(2, stacks));

    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 1));
  }

  @Test
  void aSortThatFoundEverythingTidyNamesNoCountsAtAll() {
    Text line = NetworkOperationMessages.finished(ExpensiveOperation.SORT_NETWORK, sorted(0, 0));

    assertEquals(PREFIX + "sort.network.done.tidy", contentOf(line).getKey());
  }

  @ParameterizedTest
  @CsvSource({"1, items.one", "109, items"})
  void aConsolidationCountsWhatItGatheredInTheRightNumber(long items, String key) {
    Text line =
        NetworkOperationMessages.finished(ExpensiveOperation.CONSOLIDATE, gathered(items, 2, 3));

    assertEquals(PREFIX + "consolidate.done", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 0));
  }

  @ParameterizedTest
  @CsvSource({
    "1, 1, items.one, nearby-containers.one",
    "109, 1, items, nearby-containers.one",
    "109, 4, items, nearby-containers"
  })
  void aQuickStackCountsBothItsItemsAndItsContainersInTheRightNumber(
      long items, int containers, String itemsKey, String containersKey) {
    Text line = NetworkOperationMessages.quickStacked(items, containers);

    assertEquals(PREFIX + "quickstack.done", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + itemsKey, keyOfArgument(line, 0));
    assertEquals(COUNT_PREFIX + containersKey, keyOfArgument(line, 1));
  }

  @ParameterizedTest
  @CsvSource({"1, containers.one", "7, containers"})
  void aSkippedHeadlineCountsEveryContainerTheRunLeftAlone(int containers, String key) {
    Text line =
        NetworkOperationMessages.skipped(
            new OperationReport(
                1, 1, 0, 0, 0L, containers - containers / 2, containers / 2, false));

    assertEquals(PREFIX + "skipped.headline", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 0));
  }

  @ParameterizedTest
  @CsvSource({"1, containers.one", "7, containers"})
  void aCancelledSortCountsWhatItGotThroughInTheRightNumber(int containers, String key) {
    Text line =
        NetworkOperationMessages.cancelled(ExpensiveOperation.SORT_NETWORK, sorted(containers, 0));

    assertEquals(PREFIX + "sort.network.cancelled", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 0));
  }

  @ParameterizedTest
  @CsvSource({"1, excluded-containers.one", "3, excluded-containers"})
  void aSortThatLeftContainersAloneCountsThemInTheRightNumber(int excluded, String key) {
    Text line =
        NetworkOperationMessages.finished(
            ExpensiveOperation.SORT_NETWORK,
            new OperationReport(1, 1, 2, 0, 0L, excluded, 0, false));

    assertEquals(PREFIX + "skipped.excluded", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 1));
  }

  @ParameterizedTest
  @CsvSource({"1, containers.one", "3, containers"})
  void aSortThatMetContainersItWillNotRearrangeCountsThemInTheRightNumber(
      int restricted, String key) {
    Text line =
        NetworkOperationMessages.finished(
            ExpensiveOperation.SORT_NETWORK,
            new OperationReport(1, 1, 2, 0, 0L, 0, restricted, false));

    assertEquals(PREFIX + "skipped.restricted", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + key, keyOfArgument(line, 1));
  }

  @Test
  void aSortThatSkippedBothKindsOfContainerCountsEachOfThem() {
    Text line =
        NetworkOperationMessages.finished(
            ExpensiveOperation.SORT_NETWORK, new OperationReport(1, 1, 2, 0, 0L, 1, 4, false));

    assertEquals(PREFIX + "skipped.both", contentOf(line).getKey());
    assertEquals(COUNT_PREFIX + "excluded-containers.one", keyOfArgument(line, 1));
    assertEquals(COUNT_PREFIX + "containers", keyOfArgument(line, 2));
  }
}
