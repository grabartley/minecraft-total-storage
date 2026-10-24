package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.tag.TagEdit;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TagEditPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static TagEditPayload roundTrip(TagEditPayload payload) {
    final RegistryByteBuf buffer = buffer();
    TagEditPayload.CODEC.encode(buffer, payload);
    return TagEditPayload.CODEC.decode(buffer);
  }

  @ParameterizedTest
  @EnumSource(TagEdit.class)
  void everyEditSurvivesTheWireUnchanged(TagEdit action) {
    final TagEditPayload sent =
        new TagEditPayload(7, action, "castle", "The Keep", Optional.empty());

    assertEquals(sent, roundTrip(sent));
  }

  @Test
  void creatingATagCarriesOnlyTheNameAsked() {
    final TagEditPayload payload = TagEditPayload.create(3, "Castle");

    assertEquals(TagEdit.CREATE, payload.action());
    assertEquals("Castle", payload.name());
    assertEquals("", payload.tagId());
    assertEquals(Optional.empty(), payload.item());
  }

  @Test
  void renamingNamesBothTheTagAndItsNewName() {
    final TagEditPayload payload = TagEditPayload.rename(3, "castle", "Keep");

    assertEquals(TagEdit.RENAME, payload.action());
    assertEquals("castle", payload.tagId());
    assertEquals("Keep", payload.name());
  }

  @Test
  void deletingNamesOnlyTheTag() {
    final TagEditPayload payload = TagEditPayload.delete(3, "castle");

    assertEquals(TagEdit.DELETE, payload.action());
    assertEquals("castle", payload.tagId());
    assertEquals("", payload.name());
  }

  @Test
  void assigningAndUnassigningAreDifferentActions() {
    assertEquals(TagEdit.ASSIGN, TagEditPayload.assign(3, "castle", null, true).action());
    assertEquals(TagEdit.UNASSIGN, TagEditPayload.assign(3, "castle", null, false).action());
  }

  @Test
  void assigningEverythingMatchingNamesNoItemBecauseTheServerResolvesThem() {
    final TagEditPayload payload = TagEditPayload.assignMatching(3, "castle");

    assertEquals(TagEdit.ASSIGN_MATCHING, payload.action());
    assertEquals(Optional.empty(), payload.item());
  }

  @Test
  void anEditWithoutAnActionIsRefused() {
    assertThrows(
        NullPointerException.class,
        () -> new TagEditPayload(1, null, "castle", "", Optional.empty()));
  }

  @Test
  void missingTextArrivesEmptyRatherThanNull() {
    final TagEditPayload payload = new TagEditPayload(1, TagEdit.CREATE, null, null, null);

    assertEquals("", payload.tagId());
    assertEquals("", payload.name());
    assertEquals(Optional.empty(), payload.item());
  }

  @Test
  void aNameLongerThanThePayloadAllowsIsRefusedRatherThanTruncated() {
    final TagEditPayload payload =
        new TagEditPayload(
            1,
            TagEdit.CREATE,
            "",
            "a".repeat(TagEditPayload.MAX_TEXT_LENGTH + 1),
            Optional.empty());

    assertThrows(RuntimeException.class, () -> roundTrip(payload));
  }

  @Test
  void aTagIdIsGivenRoomForANameAndItsPunctuation() {
    assertTrue(TagEditPayload.MAX_TEXT_LENGTH >= 32);
  }
}
