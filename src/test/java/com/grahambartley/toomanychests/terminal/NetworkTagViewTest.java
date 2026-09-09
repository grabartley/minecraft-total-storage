package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NetworkTagViewTest {
  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, id, true, 0, 3);
  }

  private static NetworkTagEntry custom(String id) {
    return new NetworkTagEntry(id, id, false, 2, 3);
  }

  @Test
  void aPendingViewFiltersNothingAndAdministersNothing() {
    final NetworkTagView view = NetworkTagView.pending();
    assertFalse(view.mayAdminister());
    assertEquals(List.of(), view.tags());
    assertEquals(Set.of(), view.subjectTags());
  }

  @Test
  void onlyTheEditableTagsAreOfferedAsCustom() {
    final NetworkTagView view =
        new NetworkTagView(
            true, List.of(preset("wood"), custom("castle")), Optional.empty(), Set.of());
    assertEquals(List.of(custom("castle")), view.custom());
  }

  @Test
  void aSubjectCarriesOnlyTheTagsItWasGiven() {
    final NetworkTagView view =
        new NetworkTagView(true, List.of(custom("castle")), Optional.empty(), Set.of("castle"));
    assertTrue(view.subjectCarries("castle"));
    assertFalse(view.subjectCarries("keep"));
  }

  @Test
  void aViewCarryingMoreTagsThanThePayloadAllowsIsRefused() {
    final List<NetworkTagEntry> tags = new ArrayList<>();
    for (int tag = 0; tag <= NetworkTagView.MAX_TAGS; tag++) {
      tags.add(custom("tag" + tag));
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> new NetworkTagView(true, tags, Optional.empty(), Set.of()));
  }

  @Test
  void aViewAtTheCapIsAccepted() {
    final List<NetworkTagEntry> tags = new ArrayList<>();
    for (int tag = 0; tag < NetworkTagView.MAX_TAGS; tag++) {
      tags.add(custom("tag" + tag));
    }
    assertEquals(
        NetworkTagView.MAX_TAGS,
        new NetworkTagView(true, tags, Optional.empty(), Set.of()).tags().size());
  }

  @Test
  void nullsBecomeEmptyRatherThanCrashingAScreen() {
    final NetworkTagView view = new NetworkTagView(false, null, null, null);
    assertEquals(List.of(), view.tags());
    assertEquals(Optional.empty(), view.subject());
    assertEquals(Set.of(), view.subjectTags());
  }

  @Test
  void thePromptIsOfferedTheEditableTagsBeforeTheComputedOnes() {
    final NetworkTagView view =
        new NetworkTagView(
            true,
            List.of(preset("wood"), custom("castle"), preset("stone"), custom("keep")),
            Optional.empty(),
            Set.of());

    assertEquals(
        List.of(custom("castle"), custom("keep"), preset("wood"), preset("stone")),
        view.editableFirst());
  }

  @Test
  void orderingForThePromptLosesNoTags() {
    final NetworkTagView view =
        new NetworkTagView(
            true, List.of(preset("wood"), custom("castle")), Optional.empty(), Set.of());

    assertEquals(view.tags().size(), view.editableFirst().size());
  }

  @Test
  void aNetworkWithNoCustomTagsIsOfferedItsPresetsInTheOrderTheyCame() {
    final NetworkTagView view =
        new NetworkTagView(
            true, List.of(preset("wood"), preset("stone")), Optional.empty(), Set.of());

    assertEquals(view.tags(), view.editableFirst());
  }

  @Test
  void aNetworkOfOnlyCustomTagsIsOfferedThemUnchanged() {
    final NetworkTagView view =
        new NetworkTagView(
            true, List.of(custom("castle"), custom("keep")), Optional.empty(), Set.of());

    assertEquals(view.tags(), view.editableFirst());
  }
}
