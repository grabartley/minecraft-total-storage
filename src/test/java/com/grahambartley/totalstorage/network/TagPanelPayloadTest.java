package com.grahambartley.totalstorage.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.Test;

class TagPanelPayloadTest {
  private static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  private static TagPanelPayload roundTrip(TagPanelPayload payload) {
    final RegistryByteBuf buffer = buffer();
    TagPanelPayload.CODEC.encode(buffer, payload);
    return TagPanelPayload.CODEC.decode(buffer);
  }

  private static NetworkTagEntry tag(String id, boolean immutable) {
    return new NetworkTagEntry(id, "The " + id, immutable, 3, 7);
  }

  @Test
  void everyColumnTheScreenDrawsSurvivesTheWireUnchanged() {
    final TagPanelPayload sent =
        new TagPanelPayload(
            5,
            new NetworkTagView(
                true,
                List.of(tag("wood", true), tag("castle", false)),
                Optional.empty(),
                Set.of("castle")));

    assertEquals(sent, roundTrip(sent));
  }

  @Test
  void eachTagKeepsItsNameCountsAndWhetherItCanBeEdited() {
    final TagPanelPayload received =
        roundTrip(
            new TagPanelPayload(
                5,
                new NetworkTagView(
                    false,
                    List.of(tag("wood", true), tag("castle", false)),
                    Optional.empty(),
                    Set.of())));

    final NetworkTagEntry preset = received.view().tags().get(0);
    assertEquals("wood", preset.id());
    assertEquals("The wood", preset.displayName());
    assertTrue(preset.immutable());
    assertEquals(3, preset.assignedItems());
    assertEquals(7, preset.matchingItems());
    assertFalse(received.view().tags().get(1).immutable());
    assertFalse(received.view().mayAdminister());
  }

  @Test
  void whichTagsAnItemCarriesArriveSeparatelyFromTheColumn() {
    final TagPanelPayload received =
        roundTrip(
            new TagPanelPayload(
                5,
                new NetworkTagView(
                    true,
                    List.of(tag("castle", false), tag("keep", false)),
                    Optional.empty(),
                    Set.of("keep"))));

    assertEquals(Set.of("keep"), received.view().subjectTags());
    assertEquals(2, received.view().tags().size());
  }

  @Test
  void aNetworkWithNoTagsAtAllSurvivesTheWireAsAnAnswer() {
    final TagPanelPayload sent =
        new TagPanelPayload(5, new NetworkTagView(true, List.of(), Optional.empty(), Set.of()));

    assertEquals(sent, roundTrip(sent));
    assertTrue(roundTrip(sent).view().answered());
  }

  @Test
  void aPanelNamingMoreTagsThanTheCapIsRefusedRatherThanRead() {
    final RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(5);
    buffer.writeBoolean(true);
    buffer.writeVarInt(NetworkTagView.MAX_TAGS + 1);

    assertThrows(IllegalArgumentException.class, () -> TagPanelPayload.CODEC.decode(buffer));
  }

  @Test
  void anItemNamingMoreTagsThanTheCapIsRefusedRatherThanRead() {
    final RegistryByteBuf buffer = buffer();
    buffer.writeVarInt(5);
    buffer.writeBoolean(true);
    buffer.writeVarInt(0);
    buffer.writeBoolean(false);
    buffer.writeVarInt(NetworkTagView.MAX_TAGS + 1);

    assertThrows(IllegalArgumentException.class, () -> TagPanelPayload.CODEC.decode(buffer));
  }

  @Test
  void aPanelAtTheCapIsCarriedRatherThanRefused() {
    final List<NetworkTagEntry> tags = new ArrayList<>();
    for (int index = 0; index < NetworkTagView.MAX_TAGS; index++) {
      tags.add(tag("tag" + index, false));
    }
    final TagPanelPayload sent =
        new TagPanelPayload(5, new NetworkTagView(true, tags, Optional.empty(), Set.of()));

    assertEquals(NetworkTagView.MAX_TAGS, roundTrip(sent).view().tags().size());
  }
}
