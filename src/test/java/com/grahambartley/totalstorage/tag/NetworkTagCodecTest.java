package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NetworkTagCodecTest {
  private static List<NetworkTagSet> roundTripCases() {
    return List.of(
        NetworkTagSet.defaults(),
        NetworkTagSet.defaults().withCustomTag("ores", "Ores"),
        NetworkTagSet.defaults()
            .withCustomTag("ores", "Ores")
            .withItem("ores", "minecraft:coal")
            .withItem("ores", "minecraft:raw_iron"),
        NetworkTagSet.defaults()
            .withCustomTag("ores")
            .withCustomTag("crops")
            .withItem("ores", "minecraft:coal")
            .withItem("crops", "minecraft:wheat"));
  }

  @ParameterizedTest
  @MethodSource("roundTripCases")
  void aTagSetSurvivesEncodingAndDecoding(NetworkTagSet original) {
    assertEquals(original, decode(encode(original)));
  }

  @Test
  void anItemCarryingSeveralTagsStillCarriesThemAllAfterAReload() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("ores")
            .withCustomTag("smeltable")
            .withItem("ores", "minecraft:raw_iron")
            .withItem("smeltable", "minecraft:raw_iron");

    assertEquals(Set.of("ores", "smeltable"), decode(encode(tags)).tagIdsFor("minecraft:raw_iron"));
  }

  @Test
  void onlyCustomTagsAreWrittenBecausePresetsAreRebuiltOnRead() {
    JsonArray encoded = encode(NetworkTagSet.defaults().withCustomTag("ores")).getAsJsonArray();

    assertEquals(1, encoded.size());
    assertEquals("ores", encoded.get(0).getAsJsonObject().get("id").getAsString());
  }

  @Test
  void aSaveFromABuildWithoutPresetsStillDecodesToEveryPreset() {
    assertEquals(NetworkTagSet.defaults().all(), decode(new JsonArray()).all());
  }

  @Test
  void aSavedTagWithoutANameIsNamedAfterItsId() {
    JsonArray saved = new JsonArray();
    saved.add(tagJson("ores"));

    assertEquals("ores", decode(saved).byId("ores").orElseThrow().displayName());
  }

  @Test
  void aSavedTagWithABlankIdIsDroppedRatherThanLosingTheRest() {
    JsonArray saved = new JsonArray();
    saved.add(tagJson("  "));
    saved.add(tagJson("ores"));

    assertEquals(List.of(NetworkTag.custom("ores")), decode(saved).custom());
  }

  @Test
  void aSavedTagThatCollidesWithAPresetIsDroppedRatherThanShadowingIt() {
    JsonArray saved = new JsonArray();
    saved.add(tagJson(PresetTag.WOOD.id()));

    NetworkTagSet decoded = decode(saved);

    assertTrue(decoded.custom().isEmpty());
    assertTrue(decoded.byId(PresetTag.WOOD.id()).orElseThrow().immutable());
  }

  @Test
  void twoEquallyConfiguredNetworksEncodeIdenticallyWhateverOrderBuiltThem() {
    NetworkTagSet forwards =
        NetworkTagSet.defaults()
            .withCustomTag("zinc")
            .withCustomTag("alloys")
            .withItem("zinc", "b:two")
            .withItem("zinc", "a:one");
    NetworkTagSet backwards =
        NetworkTagSet.defaults()
            .withCustomTag("alloys")
            .withCustomTag("zinc")
            .withItem("zinc", "a:one")
            .withItem("zinc", "b:two");

    assertEquals(encode(forwards).toString(), encode(backwards).toString());
  }

  private static JsonObject tagJson(String id) {
    JsonObject tag = new JsonObject();
    tag.addProperty("id", id);
    return tag;
  }

  private static JsonElement encode(NetworkTagSet tags) {
    return NetworkTagCodec.CODEC.encodeStart(JsonOps.INSTANCE, tags).getOrThrow();
  }

  private static NetworkTagSet decode(JsonElement encoded) {
    return NetworkTagCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
  }
}
