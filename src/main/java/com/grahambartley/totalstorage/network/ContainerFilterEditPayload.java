package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.filter.FilterText;
import com.grahambartley.totalstorage.tag.TagNameRule;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ContainerFilterEditPayload(
    int syncId, BlockPos container, String tagId, boolean accepted) implements CustomPayload {
  public static final CustomPayload.Id<ContainerFilterEditPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "container_filter_edit"));

  public static final PacketCodec<RegistryByteBuf, ContainerFilterEditPayload> CODEC =
      PacketCodec.of(ContainerFilterEditPayload::write, ContainerFilterEditPayload::read);

  public ContainerFilterEditPayload {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    tagId = FilterText.tagId(tagId);
  }

  @Override
  public CustomPayload.Id<ContainerFilterEditPayload> getId() {
    return ID;
  }

  private static void write(ContainerFilterEditPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBlockPos(payload.container());
    buf.writeString(payload.tagId(), TagNameRule.MAX_LENGTH);
    buf.writeBoolean(payload.accepted());
  }

  private static ContainerFilterEditPayload read(RegistryByteBuf buf) {
    return new ContainerFilterEditPayload(
        buf.readVarInt(),
        buf.readBlockPos(),
        buf.readString(TagNameRule.MAX_LENGTH),
        buf.readBoolean());
  }
}
