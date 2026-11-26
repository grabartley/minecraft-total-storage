package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ViewedInventoriesTest {
  private static final int SLOTS_EACH = 3;

  static Stream<Arguments> screens() {
    Inventory chest = mock(Inventory.class);
    Inventory left = mock(Inventory.class);
    Inventory right = mock(Inventory.class);
    Inventory player = mock(Inventory.class);
    Inventory elsewhere = mock(Inventory.class);
    DoubleInventory doubled = mock(DoubleInventory.class);
    when(doubled.isPart(left)).thenReturn(true);
    when(doubled.isPart(right)).thenReturn(true);
    return Stream.of(
        Arguments.of("a chest that changed", List.of(chest, player), Set.of(chest), true),
        Arguments.of("an unrelated chest", List.of(chest, player), Set.of(elsewhere), false),
        Arguments.of("the left half of a double", List.of(doubled, player), Set.of(left), true),
        Arguments.of("the right half of a double", List.of(doubled, player), Set.of(right), true),
        Arguments.of("a double that changed", List.of(doubled, player), Set.of(doubled), true),
        Arguments.of("a double beside another", List.of(doubled, player), Set.of(chest), false),
        Arguments.of("the player's own slots", List.of(player), Set.of(player), true),
        Arguments.of("nothing changed", List.of(chest, player), Set.of(), false),
        Arguments.of("a screen with no slots", List.of(), Set.of(chest), false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("screens")
  void aScreenShowsAChangeOnlyWhenOneOfItsInventoriesMoved(
      String screen, List<Inventory> shown, Set<Inventory> changed, boolean expected) {
    assertEquals(expected, ViewedInventories.showAny(slotsOver(shown), changed));
  }

  private static List<Slot> slotsOver(List<Inventory> inventories) {
    List<Slot> slots = new ArrayList<>();
    for (Inventory inventory : inventories) {
      for (int index = 0; index < SLOTS_EACH; index++) {
        slots.add(new Slot(inventory, index, 0, 0));
      }
    }
    return slots;
  }
}
