package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.PresetTag;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListingRequestPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static ListingRequestPayload roundTrip(ListingRequestPayload payload) {
    RegistryByteBuf buffer = buffer();
    ListingRequestPayload.CODEC.encode(buffer, payload);
    return ListingRequestPayload.CODEC.decode(buffer);
  }

  private static ListingRequestPayload request(int offset, int length, String search) {
    return new ListingRequestPayload(
        3, offset, length, search, TagCombinationMode.DEFAULT, Set.of());
  }

  @Test
  void everythingAScreenAskedForSurvivesTheWireUnchanged() {
    ListingRequestPayload sent =
        new ListingRequestPayload(
            9, 72, 36, "oak @create #wood", TagCombinationMode.OR, Set.of("wood", "storage"));

    ListingRequestPayload received = roundTrip(sent);

    assertEquals(sent, received);
  }

  @Test
  void theWholePayloadIsReadBackWithNothingLeftOver() {
    RegistryByteBuf buffer = buffer();

    ListingRequestPayload.CODEC.encode(buffer, request(40, 20, "diamond"));
    ListingRequestPayload.CODEC.decode(buffer);

    assertEquals(0, buffer.readableBytes());
  }

  @DisplayName("every combination mode is understood by name rather than by position")
  @ParameterizedTest(name = "{0}")
  @EnumSource(TagCombinationMode.class)
  void carriesEveryModeAcrossTheWire(TagCombinationMode mode) {
    ListingRequestPayload sent = new ListingRequestPayload(1, 0, 8, "", mode, Set.of());

    assertSame(mode, roundTrip(sent).mode());
  }

  @Test
  void aModeThisBuildHasNeverHeardOfFallsBackRatherThanBeingGuessed() {
    RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(1);
    buffer.writeVarInt(0);
    buffer.writeVarInt(8);
    buffer.writeString("", ListingRequestPayload.MAX_SEARCH_LENGTH);
    buffer.writeString("SIDEWAYS", ListingRequestPayload.MAX_MODE_LENGTH);
    buffer.writeVarInt(0);

    assertSame(TagCombinationMode.DEFAULT, ListingRequestPayload.CODEC.decode(buffer).mode());
  }

  @DisplayName("a window nobody could be drawing is brought back inside the bounds")
  @ParameterizedTest(name = "offset {0} length {1} becomes {2} and {3}")
  @CsvSource({"-1, -1, 0, 0", "-40, 8, 0, 8", "0, 5000, 0, 128", "0, -5000, 0, 0"})
  void clampsAWindowNoScreenCouldBeShowing(
      int offset, int length, int expectedOffset, int expectedLength) {
    ListingRequestPayload payload = request(offset, length, "");

    assertEquals(expectedOffset, payload.offset());
    assertEquals(expectedLength, payload.length());
    assertEquals(new ListingWindow(expectedOffset, expectedLength), payload.window());
  }

  @Test
  void aWindowIsTheSameWindowWhicheverSideOfTheWireItIsReadOn() {
    assertEquals(ListingWindow.of(72, 36), roundTrip(request(72, 36, "")).window());
  }

  @Test
  void aRequestThatNamedNothingIsStillAWholeRequest() {
    ListingRequestPayload payload = new ListingRequestPayload(1, 0, 8, null, null, null);

    assertEquals("", payload.search());
    assertSame(TagCombinationMode.DEFAULT, payload.mode());
    assertEquals(Set.of(), payload.tags());
  }

  @DisplayName("a tag count larger than a filter column could hold is refused while decoding")
  @ParameterizedTest(name = "{0} tags claimed")
  @ValueSource(ints = {4096, Integer.MAX_VALUE})
  void refusesAPacketClaimingMoreTagsThanMayBeNamed(int claimed) {
    RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(1);
    buffer.writeVarInt(0);
    buffer.writeVarInt(8);
    buffer.writeString("", ListingRequestPayload.MAX_SEARCH_LENGTH);
    buffer.writeString(TagCombinationMode.DEFAULT.name(), ListingRequestPayload.MAX_MODE_LENGTH);
    buffer.writeVarInt(claimed);

    assertThrows(IllegalArgumentException.class, () -> ListingRequestPayload.CODEC.decode(buffer));
  }

  @Test
  void aRequestNamingEveryTagAColumnCanOfferSurvivesTheWire() {
    final Set<String> tags = new HashSet<>();
    for (int tag = 0; tag < PresetTag.all().size() + NetworkTagSet.MAX_CUSTOM; tag++) {
      tags.add("tag" + tag);
    }
    final ListingRequestPayload sent =
        new ListingRequestPayload(1, 0, 8, "", TagCombinationMode.DEFAULT, tags);

    assertEquals(tags, roundTrip(sent).tags());
  }

  @Test
  void aRequestNamingMoreTagsThanAColumnCouldOfferIsRefusedBeforeItIsSent() {
    final Set<String> tags = new HashSet<>();
    for (int tag = 0; tag <= ListingRequestPayload.MAX_TAGS; tag++) {
      tags.add("tag" + tag);
    }

    assertThrows(
        IllegalArgumentException.class,
        () -> new ListingRequestPayload(1, 0, 8, "", TagCombinationMode.DEFAULT, tags));
  }

  @Test
  void refusesAPacketClaimingANegativeNumberOfTags() {
    RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(1);
    buffer.writeVarInt(0);
    buffer.writeVarInt(8);
    buffer.writeString("", ListingRequestPayload.MAX_SEARCH_LENGTH);
    buffer.writeString(TagCombinationMode.DEFAULT.name(), ListingRequestPayload.MAX_MODE_LENGTH);
    buffer.writeVarInt(-1);

    assertThrows(IllegalArgumentException.class, () -> ListingRequestPayload.CODEC.decode(buffer));
  }

  @Test
  void refusesSearchTextLongerThanASearchBoxCouldHold() {
    RegistryByteBuf buffer = buffer();
    String overlong = "x".repeat(ListingRequestPayload.MAX_SEARCH_LENGTH + 1);
    buffer.writeVarInt(1);
    buffer.writeVarInt(0);
    buffer.writeVarInt(8);
    buffer.writeString(overlong, overlong.length());
    buffer.writeString(TagCombinationMode.DEFAULT.name(), ListingRequestPayload.MAX_MODE_LENGTH);
    buffer.writeVarInt(0);

    assertThrows(RuntimeException.class, () -> ListingRequestPayload.CODEC.decode(buffer));
  }

  @Test
  void theTagsNamedSurviveTheWire() {
    Set<String> tags = Set.of("wood", "storage", "tools");

    ListingRequestPayload received =
        roundTrip(new ListingRequestPayload(1, 0, 8, "", TagCombinationMode.AND, tags));

    assertEquals(tags, received.tags());
    assertTrue(received.tags().contains("wood"));
  }

  @Test
  void everyPayloadReportsTheSharedChannelId() {
    assertSame(ListingRequestPayload.ID, request(0, 8, "").getId());
    assertEquals("total-storage:listing_request", ListingRequestPayload.ID.id().toString());
  }
}
