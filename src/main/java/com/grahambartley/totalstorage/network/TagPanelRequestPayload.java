package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TagPanelRequestPayload(int syncId, boolean open, Optional<ItemVariant> subject)
    implements CustomPayload {
  public static final CustomPayload.Id<TagPanelRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "tag_panel_request"));

  public static final PacketCodec<RegistryByteBuf, TagPanelRequestPayload> CODEC =
      PacketCodec.of(TagPanelRequestPayload::write, TagPanelRequestPayload::read);

  public TagPanelRequestPayload {
    subject = subject == null ? Optional.empty() : subject;
  }

  public static TagPanelRequestPayload opened(int syncId) {
    return new TagPanelRequestPayload(syncId, true, Optional.empty());
  }

  public static TagPanelRequestPayload closed(int syncId) {
    return new TagPanelRequestPayload(syncId, false, Optional.empty());
  }

  public static TagPanelRequestPayload forItem(int syncId, ItemVariant subject) {
    return new TagPanelRequestPayload(syncId, true, Optional.ofNullable(subject));
  }

  @Override
  public CustomPayload.Id<TagPanelRequestPayload> getId() {
    return ID;
  }

  private static void write(TagPanelRequestPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBoolean(payload.open());
    buf.writeBoolean(payload.subject().isPresent());
    payload.subject().ifPresent(subject -> ItemVariant.PACKET_CODEC.encode(buf, subject));
  }

  private static TagPanelRequestPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final boolean open = buf.readBoolean();
    final Optional<ItemVariant> subject =
        buf.readBoolean() ? Optional.of(ItemVariant.PACKET_CODEC.decode(buf)) : Optional.empty();
    return new TagPanelRequestPayload(syncId, open, subject);
  }
}
