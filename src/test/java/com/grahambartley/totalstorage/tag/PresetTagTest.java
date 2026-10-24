package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.totalstorage.filter.MatchStrength;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PresetTagTest {
  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void everyPresetRoundTripsThroughItsId(PresetTag preset) {
    assertEquals(Optional.of(preset), PresetTag.fromId(preset.id()));
  }

  @ParameterizedTest
  @CsvSource({
    "BUILDING_BLOCKS, building_blocks",
    "ORES_AND_MINERALS, ores_and_minerals",
    "MISCELLANEOUS, miscellaneous",
  })
  void anIdIsTheLowerCasedName(PresetTag preset, String expectedId) {
    assertEquals(expectedId, preset.id());
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void everyPresetHasItsOwnTranslationKeyUnderTheModNamespace(PresetTag preset) {
    assertEquals("tag.item.total-storage.preset." + preset.id(), preset.translationKey());
  }

  @ParameterizedTest
  @ValueSource(strings = {"  food  ", "FOOD", "Food"})
  void anIdIsMatchedIgnoringSurroundingSpaceAndCase(String id) {
    assertEquals(Optional.of(PresetTag.FOOD), PresetTag.fromId(id));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "not_a_preset", "preset/food", "tools_and_weapons"})
  void anUnknownIdResolvesToNothingRatherThanThrowing(String id) {
    assertEquals(Optional.empty(), PresetTag.fromId(id));
  }

  @Test
  void aNullIdResolvesToNothing() {
    assertEquals(Optional.empty(), PresetTag.fromId(null));
  }

  @Test
  void thePresetListCannotBeAddedTo() {
    List<PresetTag> presets = PresetTag.all();

    assertThrows(UnsupportedOperationException.class, () -> presets.add(PresetTag.FOOD));
  }

  @Test
  void thePresetListCannotBeRemovedFrom() {
    List<PresetTag> presets = PresetTag.all();

    assertThrows(UnsupportedOperationException.class, () -> presets.remove(PresetTag.FOOD));
  }

  @Test
  void thePresetListIsEveryPresetInDeclarationOrder() {
    assertEquals(List.of(PresetTag.values()), PresetTag.all());
  }

  @Test
  void theFallbackIsMiscellaneous() {
    assertSame(PresetTag.MISCELLANEOUS, PresetTag.FALLBACK);
  }

  @Test
  void theFallbackIsLastSoItSitsAtTheBottomOfTheFilterColumn() {
    List<PresetTag> presets = PresetTag.all();

    assertSame(PresetTag.FALLBACK, presets.get(presets.size() - 1));
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void noPresetSharesAnIdWithAnother(PresetTag preset) {
    long sharing = PresetTag.all().stream().filter(other -> other.id().equals(preset.id())).count();

    assertTrue(sharing == 1, preset.id() + " is used by " + sharing + " presets");
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void thePresetNameAPlayerReadsIsATermThatFindsThatPreset(PresetTag preset) {
    String typed = englishNameOf(preset).split("\\s+")[0];

    assertTrue(
        MatchStrength.of(preset.id(), typed).compareTo(MatchStrength.SUBSTRING) <= 0,
        "typing #"
            + typed
            + " only fuzzily finds it, so the name on screen is not the name to"
            + " search");
  }

  private static String englishNameOf(PresetTag preset) {
    try (InputStream lang =
        PresetTag.class.getResourceAsStream("/assets/total-storage/lang/en_us.json")) {
      assertTrue(lang != null, "the language file this mod ships was not on the classpath");
      JsonObject entries =
          JsonParser.parseReader(new InputStreamReader(lang, StandardCharsets.UTF_8))
              .getAsJsonObject();
      assertTrue(
          entries.has(preset.translationKey()), preset.translationKey() + " has no English name");
      return entries.get(preset.translationKey()).getAsString();
    } catch (IOException failure) {
      throw new AssertionError("the language file this mod ships could not be read", failure);
    }
  }
}
