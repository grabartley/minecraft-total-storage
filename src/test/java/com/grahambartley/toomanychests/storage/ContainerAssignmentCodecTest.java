package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ContainerAssignmentCodecTest {
  private static final BlockPos BARREL = new BlockPos(3, 64, -7);
  private static final BlockPos CHEST = new BlockPos(-11, 12, 40);
  private static final String COAL = "minecraft:coal";

  private static List<ContainerAssignments> roundTripCases() {
    return List.of(
        ContainerAssignments.empty(),
        ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED),
        ContainerAssignments.empty().withRole(BARREL, ContainerRole.FILTERED),
        ContainerAssignments.empty()
            .with(BARREL, ContainerAssignment.filtered(ContainerFilter.ofTags("ores"))),
        ContainerAssignments.empty()
            .with(BARREL, ContainerAssignment.filtered(ContainerFilter.ofItems(COAL))),
        ContainerAssignments.empty()
            .with(
                BARREL,
                ContainerAssignment.filtered(
                    ContainerFilter.of(List.of("ores", "wood"), List.of(COAL, "minecraft:stone"))))
            .withRole(CHEST, ContainerRole.EXCLUDED));
  }

  @ParameterizedTest
  @MethodSource("roundTripCases")
  void assignmentsSurviveEncodingAndDecoding(ContainerAssignments original) {
    assertEquals(original, decode(encode(original)));
  }

  @Test
  void anEmptySaveDecodesToANetworkNobodyConfigured() {
    assertEquals(ContainerAssignments.empty(), decode(new JsonArray()));
  }

  @Test
  void anUnrecognisedRoleIsReadAsTheDefaultAndForgotten() {
    JsonArray tampered =
        encode(ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED))
            .getAsJsonArray();
    tampered.get(0).getAsJsonObject().addProperty("role", "readonly");

    assertTrue(decode(tampered).isEmpty());
  }

  @Test
  void anEntryWithNoRoleAtAllIsForgottenRatherThanLosingTheWholeSave() {
    JsonArray tampered =
        encode(
                ContainerAssignments.empty()
                    .withRole(BARREL, ContainerRole.EXCLUDED)
                    .withRole(CHEST, ContainerRole.FILTERED))
            .getAsJsonArray();
    tampered.get(0).getAsJsonObject().remove("role");

    assertEquals(1, decode(tampered).size());
  }

  /** The shape older builds wrote, before a filtered container had anything to store. */
  @Test
  void aSaveWrittenBeforeFiltersExistedStillReadsItsRoles() {
    JsonObject legacy = new JsonObject();
    JsonArray pos = new JsonArray();
    pos.add(BARREL.getX());
    pos.add(BARREL.getY());
    pos.add(BARREL.getZ());
    legacy.add("pos", pos);
    legacy.addProperty("role", "excluded");
    JsonArray save = new JsonArray();
    save.add(legacy);

    assertEquals(ContainerRole.EXCLUDED, decode(save).roleFor(BARREL));
  }

  @Test
  void aFilterStoredAgainstARoleThatCannotUseOneIsDroppedOnRead() {
    JsonArray tampered =
        encode(ContainerAssignments.empty().withRole(BARREL, ContainerRole.EXCLUDED))
            .getAsJsonArray();
    JsonArray tags = new JsonArray();
    tags.add("ores");
    tampered.get(0).getAsJsonObject().add("tags", tags);

    assertEquals(ContainerFilter.NONE, decode(tampered).filterFor(BARREL));
  }

  @Test
  void aBlankSavedTagIsDroppedOnRead() {
    JsonArray tampered =
        encode(
                ContainerAssignments.empty()
                    .with(BARREL, ContainerAssignment.filtered(ContainerFilter.ofTags("ores"))))
            .getAsJsonArray();
    tampered.get(0).getAsJsonObject().getAsJsonArray("tags").add("   ");

    assertEquals(ContainerFilter.ofTags("ores"), decode(tampered).filterFor(BARREL));
  }

  @Test
  void twoEquallyConfiguredNetworksEncodeIdenticallyWhateverOrderBuiltThem() {
    ContainerAssignments forwards =
        ContainerAssignments.empty()
            .withRole(BARREL, ContainerRole.EXCLUDED)
            .with(BARREL, ContainerAssignment.DEFAULT)
            .withRole(CHEST, ContainerRole.FILTERED)
            .withFilter(CHEST, ContainerFilter.NONE.withTag("wood").withTag("ores"));
    ContainerAssignments backwards =
        ContainerAssignments.empty()
            .with(CHEST, ContainerAssignment.filtered(ContainerFilter.ofTags("ores", "wood")));

    assertEquals(encode(forwards).toString(), encode(backwards).toString());
  }

  private static JsonElement encode(ContainerAssignments assignments) {
    return ContainerAssignmentCodec.CODEC.encodeStart(JsonOps.INSTANCE, assignments).getOrThrow();
  }

  private static ContainerAssignments decode(JsonElement encoded) {
    return ContainerAssignmentCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
  }
}
