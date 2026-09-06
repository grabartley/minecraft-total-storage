package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkTagTest {
  @Test
  void aCustomTagWithoutANameIsNamedAfterItsId() {
    assertEquals("ores", NetworkTag.custom("ores").displayName());
    assertEquals("ores", NetworkTag.custom("ores", "  ").displayName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ORES", "  ores  ", "Ores"})
  void anIdIsFoldedSoTheSameTagIsNeverTwoTags(String given) {
    assertEquals("ores", NetworkTag.custom(given).id());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  void aTagWithoutAnIdIsRefusedRatherThanStoredNameless(String id) {
    assertThrows(IllegalArgumentException.class, () -> NetworkTag.custom(id));
  }

  @Test
  void aNullIdIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> NetworkTag.custom(null));
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void everyPresetIsImmutableAndNamedByTranslationKey(PresetTag preset) {
    NetworkTag tag = NetworkTag.preset(preset);

    assertTrue(tag.immutable());
    assertEquals(preset.id(), tag.id());
    assertEquals(preset.translationKey(), tag.displayName());
    assertTrue(tag.items().isEmpty());
  }

  @Test
  void anItemIsRememberedAndCanBeRemoved() {
    NetworkTag tag = NetworkTag.custom("ores").withItem("minecraft:raw_iron");

    assertTrue(tag.contains("minecraft:raw_iron"));
    assertFalse(tag.withoutItem("minecraft:raw_iron").contains("minecraft:raw_iron"));
  }

  @Test
  void anItemIdIsFoldedOnStorageAndOnLookup() {
    NetworkTag tag = NetworkTag.custom("ores").withItem("  Minecraft:Raw_Iron ");

    assertEquals(Set.of("minecraft:raw_iron"), tag.items());
    assertTrue(tag.contains("MINECRAFT:RAW_IRON"));
  }

  @Test
  void assigningTheSameItemTwiceChangesNothing() {
    NetworkTag tag = NetworkTag.custom("ores").withItem("minecraft:coal");

    assertSame(tag, tag.withItem("minecraft:coal"));
    assertSame(tag, tag.withoutItem("minecraft:diamond"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aBlankItemIsNotAnAssignment(String itemId) {
    NetworkTag tag = NetworkTag.custom("ores");

    assertSame(tag, tag.withItem(itemId));
  }

  @Test
  void aNullItemIsIgnoredOnBothSides() {
    NetworkTag tag = NetworkTag.custom("ores");

    assertSame(tag, tag.withItem(null));
    assertSame(tag, tag.withoutItem(null));
    assertFalse(tag.contains(null));
  }

  @Test
  void aPresetCannotBeAssignedItemsRenamedOrEmptied() {
    NetworkTag preset = NetworkTag.preset(PresetTag.ORES_AND_MINERALS);

    assertSame(preset, preset.withItem("minecraft:raw_iron"));
    assertSame(preset, preset.withoutItem("minecraft:raw_iron"));
    assertSame(preset, preset.withDisplayName("My Ores"));
  }

  @Test
  void aCustomTagCanBeRenamedButNotToNothing() {
    NetworkTag tag = NetworkTag.custom("ores");

    assertEquals("Shiny Rocks", tag.withDisplayName("  Shiny Rocks  ").displayName());
    assertSame(tag, tag.withDisplayName(null));
    assertSame(tag, tag.withDisplayName("   "));
  }

  @Test
  void nullsAndBlanksInsideAnItemListAreDroppedRatherThanStored() {
    NetworkTag tag =
        NetworkTag.custom("ores", "Ores", Arrays.asList("minecraft:coal", null, "  ", "COAL"));

    assertEquals(Set.of("minecraft:coal", "coal"), tag.items());
  }

  @Test
  void itemsAreStoredInAStableOrderWhateverOrderTheyArrivedIn() {
    NetworkTag forwards = NetworkTag.custom("ores", "Ores", List.of("b:two", "a:one"));
    NetworkTag backwards = NetworkTag.custom("ores", "Ores", List.of("a:one", "b:two"));

    assertEquals(List.copyOf(forwards.items()), List.copyOf(backwards.items()));
    assertEquals(forwards, backwards);
  }

  @Test
  void storedItemsRefuseToBeChangedFromOutside() {
    NetworkTag tag = NetworkTag.custom("ores").withItem("minecraft:coal");

    assertThrows(UnsupportedOperationException.class, () -> tag.items().add("minecraft:diamond"));
  }
}
