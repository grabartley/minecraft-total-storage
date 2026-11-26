package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HeldResourcesTest {
  private record Slot(String resource, long amount, boolean blank) {}

  static Stream<Arguments> containers() {
    return Stream.of(
        Arguments.of("an empty container", List.of(), Set.of()),
        Arguments.of(
            "one kind in one slot", List.of(new Slot("diamond", 8L, false)), Set.of("diamond")),
        Arguments.of(
            "one kind over several slots",
            List.of(new Slot("diamond", 64L, false), new Slot("diamond", 3L, false)),
            Set.of("diamond")),
        Arguments.of(
            "two kinds",
            List.of(new Slot("diamond", 8L, false), new Slot("coal", 1L, false)),
            Set.of("diamond", "coal")),
        Arguments.of("a blank view", List.of(new Slot("air", 0L, true)), Set.of()),
        Arguments.of("a view holding none", List.of(new Slot("coal", 0L, false)), Set.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("containers")
  void aContainerHoldsEveryKindInANonEmptySlot(String label, List<Slot> slots, Set<String> held) {
    assertEquals(held, HeldResources.of(storageOf(slots)));
  }

  @Test
  void noStorageHoldsNothing() {
    assertEquals(Set.of(), HeldResources.of(null));
  }

  @SuppressWarnings("unchecked")
  private static Storage<String> storageOf(List<Slot> slots) {
    List<StorageView<String>> views = new ArrayList<>();
    for (Slot slot : slots) {
      StorageView<String> view = mock(StorageView.class);
      when(view.getResource()).thenReturn(slot.resource());
      when(view.getAmount()).thenReturn(slot.amount());
      when(view.isResourceBlank()).thenReturn(slot.blank());
      views.add(view);
    }
    Storage<String> storage = mock(Storage.class);
    when(storage.nonEmptyViews()).thenReturn(views);
    return storage;
  }
}
