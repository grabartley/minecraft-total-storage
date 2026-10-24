package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.tag.TagSetImportMode;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record TagSetCopyPayload(int syncId, BlockPos source, TagSetImportMode mode)
    implements CustomPayload {
  public static final CustomPayload.Id<TagSetCopyPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "tag_set_copy"));

  public static final PacketCodec<RegistryByteBuf, TagSetCopyPayload> CODEC =
      PacketCodec.of(TagSetCopyPayload::write, TagSetCopyPayload::read);

  public TagSetCopyPayload {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(mode, "mode");
    source = source.toImmutable();
  }

  @Override
  public CustomPayload.Id<TagSetCopyPayload> getId() {
    return ID;
  }

  private static void write(TagSetCopyPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeBlockPos(payload.source());
    buf.writeEnumConstant(payload.mode());
  }

  private static TagSetCopyPayload read(RegistryByteBuf buf) {
    return new TagSetCopyPayload(
        buf.readVarInt(), buf.readBlockPos(), buf.readEnumConstant(TagSetImportMode.class));
  }
}
