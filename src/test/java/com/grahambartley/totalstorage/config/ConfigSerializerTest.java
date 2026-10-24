package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigSerializerTest {
  @Test
  void everyFieldSurvivesARoundTrip() {
    TotalStorageConfig original = TestConfigs.everythingNonDefault();

    assertEquals(original, ConfigSerializer.fromJson(ConfigSerializer.toJson(original)));
  }

  @Test
  void anEmptyDocumentYieldsDefaults() {
    assertEquals(TotalStorageConfig.defaults(), ConfigSerializer.fromJson(new JsonObject()));
  }

  @Test
  void aMissingDocumentYieldsDefaults() {
    assertEquals(TotalStorageConfig.defaults(), ConfigSerializer.fromJson(null));
  }

  @Test
  void aDocumentFromAnOlderVersionKeepsTheKeysItStillHas() {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_MAX_TERMINAL_RADIUS, 24);

    TotalStorageConfig loaded = ConfigSerializer.fromJson(root);

    assertEquals(24, loaded.maxTerminalRadius());
    assertEquals(TotalStorageConfig.DEFAULT_QUICK_STACK_RADIUS, loaded.quickStackRadius());
  }

  @ParameterizedTest
  @CsvSource({"0, 1", "999, 64", "24, 24"})
  void anOutOfRangeRadiusIsClampedRatherThanRejected(int given, int expected) {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_MAX_TERMINAL_RADIUS, given);

    assertEquals(expected, ConfigSerializer.fromJson(root).maxTerminalRadius());
  }

  @ParameterizedTest
  @ValueSource(strings = {"admin", "", "owner only"})
  void anUnrecognisedAccessModeFallsBackToTheDefault(String given) {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_DEFAULT_TERMINAL_ACCESS_MODE, given);

    assertEquals(
        TotalStorageConfig.DEFAULT_TERMINAL_ACCESS_MODE,
        ConfigSerializer.fromJson(root).defaultTerminalAccessMode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"alphabetical", "", "by name"})
  void anUnrecognisedSortOrderingFallsBackToTheDefault(String given) {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_SORT_ORDERING, given);

    assertEquals(
        TotalStorageConfig.DEFAULT_SORT_ORDERING, ConfigSerializer.fromJson(root).sortOrdering());
  }

  @Test
  void aNonNumericRadiusFallsBackToTheDefault() {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_MAX_TERMINAL_RADIUS, "very big");

    assertEquals(
        TotalStorageConfig.DEFAULT_MAX_TERMINAL_RADIUS,
        ConfigSerializer.fromJson(root).maxTerminalRadius());
  }

  @Test
  void eligibilityOverridesThatAreNotAnObjectAreIgnored() {
    JsonObject root = new JsonObject();
    root.addProperty(ConfigSerializer.KEY_CONTAINER_ELIGIBILITY_OVERRIDES, "minecraft:furnace");

    assertTrue(ConfigSerializer.fromJson(root).containerEligibilityOverrides().isEmpty());
  }

  @Test
  void aNonBooleanOverrideEntryIsDroppedWithoutLosingTheRest() {
    JsonObject overrides = new JsonObject();
    overrides.add("minecraft:hopper", new JsonObject());
    overrides.addProperty("minecraft:furnace", true);
    JsonObject root = new JsonObject();
    root.add(ConfigSerializer.KEY_CONTAINER_ELIGIBILITY_OVERRIDES, overrides);

    assertEquals(
        Map.of("minecraft:furnace", true),
        ConfigSerializer.fromJson(root).containerEligibilityOverrides());
  }

  @Test
  void enumsArePersistedAsReadableIds() {
    JsonObject written = ConfigSerializer.toJson(TestConfigs.everythingNonDefault());

    assertEquals(
        "open", written.get(ConfigSerializer.KEY_DEFAULT_TERMINAL_ACCESS_MODE).getAsString());
    assertEquals("display_name", written.get(ConfigSerializer.KEY_SORT_ORDERING).getAsString());
  }
}
