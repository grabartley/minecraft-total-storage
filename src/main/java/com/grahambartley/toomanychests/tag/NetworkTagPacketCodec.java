package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public final class NetworkTagPacketCodec {
  public static final PacketCodec<RegistryByteBuf, NetworkTagSet> INSTANCE =
      PacketCodec.of(NetworkTagPacketCodec::write, NetworkTagPacketCodec::read);

  private NetworkTagPacketCodec() {}

  private static void write(NetworkTagSet tags, RegistryByteBuf buf) {
    buf.writeCollection(
        tags.custom(),
        (target, tag) -> {
          target.writeString(tag.id());
          target.writeString(tag.displayName());
          target.writeCollection(tag.items(), PacketByteBuf::writeString);
        });
  }

  private static NetworkTagSet read(RegistryByteBuf buf) {
    final List<NetworkTag> tags = new ArrayList<>();
    for (final NetworkTag tag :
        buf.readList(
            source -> {
              final String id = source.readString();
              final String displayName = source.readString();
              final List<String> items = source.readList(PacketByteBuf::readString);
              return FilterText.isBlank(id) ? null : NetworkTag.custom(id, displayName, items);
            })) {
      if (tag != null) {
        tags.add(tag);
      }
    }
    return new NetworkTagSet(tags);
  }
}
