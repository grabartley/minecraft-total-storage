package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.terminal.OwnedTerminal;
import com.grahambartley.totalstorage.terminal.OwnedTerminals;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TagSourcesPayload(int syncId, List<OwnedTerminal> sources) implements CustomPayload {
  public static final CustomPayload.Id<TagSourcesPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "tag_sources"));

  public static final PacketCodec<RegistryByteBuf, TagSourcesPayload> CODEC =
      PacketCodec.of(TagSourcesPayload::write, TagSourcesPayload::read);

  public TagSourcesPayload {
    sources = sources == null ? List.of() : List.copyOf(sources);
    if (sources.size() > OwnedTerminals.MAX_LISTED) {
      throw new IllegalArgumentException("a tag source list may not carry " + sources.size());
    }
  }

  @Override
  public CustomPayload.Id<TagSourcesPayload> getId() {
    return ID;
  }

  private static void write(TagSourcesPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeVarInt(payload.sources().size());
    for (final OwnedTerminal source : payload.sources()) {
      buf.writeBlockPos(source.pos());
      buf.writeVarInt(source.customTags());
    }
  }

  private static TagSourcesPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final int count = buf.readVarInt();
    if (count < 0 || count > OwnedTerminals.MAX_LISTED) {
      throw new IllegalArgumentException("a tag source list named " + count + " terminals");
    }
    final List<OwnedTerminal> sources = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      sources.add(new OwnedTerminal(buf.readBlockPos(), buf.readVarInt()));
    }
    return new TagSourcesPayload(syncId, sources);
  }
}
