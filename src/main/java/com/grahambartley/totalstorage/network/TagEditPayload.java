package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.tag.TagEdit;
import com.grahambartley.totalstorage.tag.TagNameRule;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TagEditPayload(
    int syncId, TagEdit action, String tagId, String name, Optional<ItemVariant> item)
    implements CustomPayload {

  public static final int MAX_TEXT_LENGTH = TagNameRule.MAX_LENGTH * 2;

  public static final CustomPayload.Id<TagEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "tag_edit"));

  public static final PacketCodec<RegistryByteBuf, TagEditPayload> CODEC =
      PacketCodec.of(TagEditPayload::write, TagEditPayload::read);

  public TagEditPayload {
    Objects.requireNonNull(action, "action");
    tagId = tagId == null ? "" : tagId;
    name = name == null ? "" : name;
    item = item == null ? Optional.empty() : item;
  }

  public static TagEditPayload create(int syncId, String name) {
    return new TagEditPayload(syncId, TagEdit.CREATE, "", name, Optional.empty());
  }

  public static TagEditPayload rename(int syncId, String tagId, String name) {
    return new TagEditPayload(syncId, TagEdit.RENAME, tagId, name, Optional.empty());
  }

  public static TagEditPayload delete(int syncId, String tagId) {
    return new TagEditPayload(syncId, TagEdit.DELETE, tagId, "", Optional.empty());
  }

  public static TagEditPayload assign(
      int syncId, String tagId, ItemVariant item, boolean assigned) {
    return new TagEditPayload(
        syncId, assigned ? TagEdit.ASSIGN : TagEdit.UNASSIGN, tagId, "", Optional.ofNullable(item));
  }

  public static TagEditPayload assignMatching(int syncId, String tagId) {
    return new TagEditPayload(syncId, TagEdit.ASSIGN_MATCHING, tagId, "", Optional.empty());
  }

  @Override
  public CustomPayload.Id<TagEditPayload> getId() {
    return ID;
  }

  private static void write(TagEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.action());
    buf.writeString(payload.tagId(), MAX_TEXT_LENGTH);
    buf.writeString(payload.name(), MAX_TEXT_LENGTH);
    buf.writeBoolean(payload.item().isPresent());
    payload.item().ifPresent(item -> ItemVariant.PACKET_CODEC.encode(buf, item));
  }

  private static TagEditPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final TagEdit action = buf.readEnumConstant(TagEdit.class);
    final String tagId = buf.readString(MAX_TEXT_LENGTH);
    final String name = buf.readString(MAX_TEXT_LENGTH);
    final Optional<ItemVariant> item =
        buf.readBoolean() ? Optional.of(ItemVariant.PACKET_CODEC.decode(buf)) : Optional.empty();
    return new TagEditPayload(syncId, action, tagId, name, item);
  }
}
