package com.grahambartley.totalstorage.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SortFeedbackTest {
  @ParameterizedTest
  @EnumSource(SortOutcome.class)
  void everyOutcomeSaysSomethingBackToThePlayer(SortOutcome outcome) {
    assertNotNull(SortFeedback.messageFor(outcome));
  }

  @Test
  void anOutcomeTheReaderMustTellApartReadsDifferently() {
    final Set<String> wordings = new HashSet<>();
    for (final SortOutcome outcome : SortOutcome.values()) {
      wordings.add(SortFeedback.messageFor(outcome).getString());
    }

    assertEquals(SortOutcome.values().length - 1, wordings.size());
  }

  @Test
  void aScreenThatCannotBeSortedReadsTheSameAsNoScreenAtAll() {
    assertEquals(
        SortFeedback.messageFor(SortOutcome.NO_OPEN_SCREEN).getString(),
        SortFeedback.messageFor(SortOutcome.SCREEN_NOT_SORTABLE).getString());
  }

  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {"SORTED", "ALREADY_SORTED", "NOTHING_TO_SORT"})
  void anInventorySortedInsteadOfAContainerSaysSo(SortOutcome outcome) {
    final String inventory =
        SortFeedback.messageFor(new SortReply(SortSubject.INVENTORY, outcome)).getString();

    assertNotEquals(SortFeedback.messageFor(outcome).getString(), inventory);
    assertTrue(inventory.contains("inventory"), inventory);
  }

  @ParameterizedTest
  @EnumSource(
      value = SortOutcome.class,
      names = {"SORTED", "ALREADY_SORTED", "NOTHING_TO_SORT"},
      mode = EnumSource.Mode.EXCLUDE)
  void anInventoryOutcomeThatSortedNothingReadsAsItAlwaysDoes(SortOutcome outcome) {
    assertEquals(
        SortFeedback.messageFor(outcome).getString(),
        SortFeedback.messageFor(new SortReply(SortSubject.INVENTORY, outcome)).getString());
  }

  @ParameterizedTest
  @EnumSource(value = SortSubject.class, names = "INVENTORY", mode = EnumSource.Mode.EXCLUDE)
  void aContainerReadsAsItAlwaysDoesWhetherOpenOrInView(SortSubject subject) {
    for (final SortOutcome outcome : SortOutcome.values()) {
      assertEquals(
          SortFeedback.messageFor(outcome).getString(),
          SortFeedback.messageFor(new SortReply(subject, outcome)).getString());
    }
  }
}
