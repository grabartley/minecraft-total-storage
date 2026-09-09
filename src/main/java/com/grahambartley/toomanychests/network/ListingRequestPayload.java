package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ListingRequestPayload(
    int syncId, int offset, int length, String search, TagCombinationMode mode, Set<String> tags)
    implements CustomPayload {

  public static final int MAX_TAGS = NetworkTagSet.MAX_TOTAL;

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
    if (tags.size() > MAX_TAGS) {
      throw new IllegalArgumentException("a listing request may not name " + tags.size() + " tags");
    }
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
    final TagCombinationMode mode =
        TagCombinationMode.fromName(buf.readString(MAX_MODE_LENGTH))
            .orElse(TagCombinationMode.DEFAULT);
    final int tagCount = buf.readVarInt();
    if (tagCount < 0 || tagCount > MAX_TAGS) {
      throw new IllegalArgumentException("listing request named " + tagCount + " tags");
    }
    final Set<String> tags = new HashSet<>();
    for (int tag = 0; tag < tagCount; tag++) {
      tags.add(buf.readString(MAX_TAG_LENGTH));
    }
    return new ListingRequestPayload(syncId, offset, length, search, mode, tags);
  }
}
