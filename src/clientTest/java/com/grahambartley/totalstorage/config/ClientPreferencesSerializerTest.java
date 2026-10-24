package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClientPreferencesSerializerTest {
  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void everySettingSurvivesTheRoundTripThroughJson(ScreenButtonAnchor anchor) {
    final ClientPreferences written = new ClientPreferences(false, anchor, false);

    assertEquals(
        written, ClientPreferencesSerializer.fromJson(ClientPreferencesSerializer.toJson(written)));
  }

  @Test
  void anEmptyFileReadsAsTheDefaults() {
    assertEquals(
        ClientPreferences.defaults(), ClientPreferencesSerializer.fromJson(new JsonObject()));
    assertEquals(ClientPreferences.defaults(), ClientPreferencesSerializer.fromJson(null));
  }

  @Test
  void anAnchorNobodyShipsFallsBackRatherThanFailing() {
    final JsonObject root = new JsonObject();
    root.addProperty("screenButtonAnchor", "middle_of_the_screen");

    assertEquals(
        ClientPreferences.DEFAULT_SCREEN_BUTTON_ANCHOR,
        ClientPreferencesSerializer.fromJson(root).screenButtonAnchor());
  }

  @Test
  void aSettingOfTheWrongShapeFallsBackRatherThanFailing() {
    final JsonObject root =
        JsonParser.parseString("{\"sortButtonVisible\": {\"on\": true}}").getAsJsonObject();

    assertEquals(
        ClientPreferences.DEFAULT_SORT_BUTTON_VISIBLE,
        ClientPreferencesSerializer.fromJson(root).sortButtonVisible());
  }

  @Test
  void aFileWrittenOutNamesEverySetting() {
    final JsonObject written = ClientPreferencesSerializer.toJson(ClientPreferences.defaults());

    assertEquals(3, written.size());
  }
}
