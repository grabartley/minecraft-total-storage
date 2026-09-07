package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A screen asking for the slice of the listing it is about to draw.
 *
 * <p>Per ADR 0009 this carries what the player did rather than what the server should return: the
 * rows they scrolled to, the text they typed, and the tags they clicked. The server parses the
 * search and evaluates it against the live index, so naming a variant differently cannot conjure
 * one the terminal does not hold.
 *
 * <p>The sync id names which screen is asking, and is checked against the handler the server
 * already holds for that player rather than trusted, so the request cannot reach a terminal the
 * sender does not have open.
 *
 * <p>Every bound is enforced while decoding rather than after it. A length or a tag count is a
 * promise about how much follows, and a promise a client makes about the size of its own packet is
 * the one thing that must be checked before any of it is allocated.
 */
public record ListingRequestPayload(
    int syncId, int offset, int length, String search, TagCombinationMode mode, Set<String> tags)
    implements CustomPayload {

  /** A ceiling on the tags one request may name, well past what the filter column can show. */
  public static final int MAX_TAGS = 64;

  public static final int MAX_SEARCH_LENGTH = 256;
  public static final int MAX_TAG_LENGTH = 64;
  public static final int MAX_MODE_LENGTH = 32;

  public static final CustomPayload.Id<ListingRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "listing_request"));

  public static final PacketCodec<RegistryByteBuf, ListingRequestPayload> CODEC =
      PacketCodec.of(ListingRequestPayload::write, ListingRequestPayload::read);

  public ListingRequestPayload {
    offset = Math.max(0, offset);
    length = Math.max(0, Math.min(ListingWindow.MAX_LENGTH, length));
    search = search == null ? "" : search;
    mode = mode == null ? TagCombinationMode.DEFAULT : mode;
    tags = tags == null ? Set.of() : Set.copyOf(tags);
  }

  public ListingWindow window() {
    return new ListingWindow(offset, length);
  }

  @Override
  public CustomPayload.Id<ListingRequestPayload> getId() {
    return ID;
  }

  private static void write(ListingRequestPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeVarInt(payload.offset());
    buf.writeVarInt(payload.length());
    buf.writeString(payload.search(), MAX_SEARCH_LENGTH);
    buf.writeString(payload.mode().name(), MAX_MODE_LENGTH);
    buf.writeVarInt(payload.tags().size());
    for (final String tag : payload.tags()) {
      buf.writeString(tag, MAX_TAG_LENGTH);
    }
  }

  private static ListingRequestPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final int offset = buf.readVarInt();
    final int length = buf.readVarInt();
    final String search = buf.readString(MAX_SEARCH_LENGTH);
    final TagCombinationMode mode = modeNamed(buf.readString(MAX_MODE_LENGTH));
    final int tagCount = buf.readVarInt();
    if (tagCount < 0 || tagCount > MAX_TAGS) {
      throw new IllegalArgumentException("listing request named " + tagCount + " tags");
    }
    final Set<String> tags = new LinkedHashSet<>();
    for (int tag = 0; tag < tagCount; tag++) {
      tags.add(buf.readString(MAX_TAG_LENGTH));
    }
    return new ListingRequestPayload(syncId, offset, length, search, mode, tags);
  }

  /**
   * Modes travel by name rather than by ordinal, as the config codec does, so reordering the enum
   * cannot quietly turn one mode into the other for a client on a different build.
   */
  private static TagCombinationMode modeNamed(String name) {
    for (final TagCombinationMode candidate : TagCombinationMode.values()) {
      if (candidate.name().equalsIgnoreCase(name)) {
        return candidate;
      }
    }
    return TagCombinationMode.DEFAULT;
  }
}
