package com.grahambartley.toomanychests.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LockedSlotsCodecTest {
  private static List<LockedSlots> roundTripCases() {
    return List.of(
        LockedSlots.empty(),
        LockedSlots.of(List.of(0)),
        LockedSlots.of(List.of(35)),
        LockedSlots.of(List.of(0, 8, 9, 35)),
        LockedSlots.of(List.of(2, 14, 27)));
  }

  @ParameterizedTest
  @MethodSource("roundTripCases")
  void aSetSurvivesEncodingAndDecoding(LockedSlots original) {
    JsonElement encoded =
        LockedSlotsCodec.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
    LockedSlots decoded = LockedSlotsCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

    assertEquals(original, decoded);
  }

  @Test
  void anEncodedSetIsAnAscendingListOfIndices() {
    JsonElement encoded =
        LockedSlotsCodec.CODEC
            .encodeStart(JsonOps.INSTANCE, LockedSlots.of(List.of(30, 2, 17)))
            .getOrThrow();

    assertEquals("[2,17,30]", encoded.toString());
  }

  @Test
  void twoEqualSetsEncodeIdenticallyWhateverOrderBuiltThem() {
    LockedSlots ascending = LockedSlots.empty().with(3).with(11);
    LockedSlots descending = LockedSlots.empty().with(11).with(3);

    assertEquals(
        LockedSlotsCodec.CODEC.encodeStart(JsonOps.INSTANCE, ascending).getOrThrow().toString(),
        LockedSlotsCodec.CODEC.encodeStart(JsonOps.INSTANCE, descending).getOrThrow().toString());
  }

  @Test
  void storedIndicesOutsideTheInventoryAreDroppedOnRead() {
    JsonElement tampered =
        LockedSlotsCodec.CODEC
            .encodeStart(JsonOps.INSTANCE, LockedSlots.of(List.of(4)))
            .getOrThrow();
    tampered.getAsJsonArray().add(9999);
    tampered.getAsJsonArray().add(-1);

    LockedSlots decoded = LockedSlotsCodec.CODEC.parse(JsonOps.INSTANCE, tampered).getOrThrow();

    assertEquals(LockedSlots.of(List.of(4)), decoded);
  }

  @Test
  void anEmptySetDecodesBackToNoLocks() {
    JsonElement encoded =
        LockedSlotsCodec.CODEC.encodeStart(JsonOps.INSTANCE, LockedSlots.empty()).getOrThrow();

    assertTrue(LockedSlotsCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().isEmpty());
  }
}
