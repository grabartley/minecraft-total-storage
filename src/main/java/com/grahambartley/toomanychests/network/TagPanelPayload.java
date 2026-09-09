package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TagPanelPayload(int syncId, NetworkTagView view) implements CustomPayload {
  public static final CustomPayload.Id<TagPanelPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "tag_panel"));

  public static final PacketCodec<RegistryByteBuf, TagPanelPayload> CODEC =
      PacketCodec.of(TagPanelPayload::write, TagPanelPayload::read);

  private static final int MAX_TEXT_LENGTH = 128;

  public TagPanelPayload {
    Objects.requireNonNull(view, "view");
  }

  @Override
  public CustomPayload.Id<TagPanelPayload> getId() {
    return ID;
  }

  private static void write(TagPanelPayload payload, RegistryByteBuf buf) {
    final NetworkTagView view = payload.view();
    buf.writeVarInt(payload.syncId());
    buf.writeBoolean(view.mayAdminister());
    buf.writeVarInt(view.tags().size());
    for (final NetworkTagEntry tag : view.tags()) {
      buf.writeString(tag.id(), MAX_TEXT_LENGTH);
      buf.writeString(tag.displayName(), MAX_TEXT_LENGTH);
      buf.writeBoolean(tag.immutable());
      buf.writeVarInt(tag.assignedItems());
      buf.writeVarInt(tag.matchingItems());
    }
    buf.writeBoolean(view.subject().isPresent());
    view.subject().ifPresent(subject -> ItemVariant.PACKET_CODEC.encode(buf, subject));
    buf.writeVarInt(view.subjectTags().size());
    for (final String tagId : view.subjectTags()) {
      buf.writeString(tagId, MAX_TEXT_LENGTH);
    }
  }

  private static TagPanelPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final boolean mayAdminister = buf.readBoolean();
    final List<NetworkTagEntry> tags = readTags(buf);
    final Optional<ItemVariant> subject =
        buf.readBoolean() ? Optional.of(ItemVariant.PACKET_CODEC.decode(buf)) : Optional.empty();
    final Set<String> subjectTags = readSubjectTags(buf);
    return new TagPanelPayload(
        syncId, new NetworkTagView(mayAdminister, tags, subject, subjectTags));
  }

  private static List<NetworkTagEntry> readTags(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > NetworkTagView.MAX_TAGS) {
      throw new IllegalArgumentException("tag panel carried " + count + " tags");
    }
    final List<NetworkTagEntry> tags = new ArrayList<>(count);
    for (int tag = 0; tag < count; tag++) {
      tags.add(
          new NetworkTagEntry(
              buf.readString(MAX_TEXT_LENGTH),
              buf.readString(MAX_TEXT_LENGTH),
              buf.readBoolean(),
              buf.readVarInt(),
              buf.readVarInt()));
    }
    return tags;
  }

  private static Set<String> readSubjectTags(RegistryByteBuf buf) {
    final int count = buf.readVarInt();
    if (count < 0 || count > NetworkTagView.MAX_TAGS) {
      throw new IllegalArgumentException("tag panel named " + count + " tags on one item");
    }
    final Set<String> tags = new LinkedHashSet<>(count);
    for (int tag = 0; tag < count; tag++) {
      tags.add(buf.readString(MAX_TEXT_LENGTH));
    }
    return tags;
  }
}
