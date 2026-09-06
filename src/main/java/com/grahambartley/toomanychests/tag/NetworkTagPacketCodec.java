package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

/**
 * The wire form of a network's tag set, for handing the terminal screen the vocabulary it renders
 * the filter column from.
 *
 * <p>Presets do not travel. A client resolves them from the same compiled-in enum the server used,
 * so sending them would be sending the client something it already has.
 *
 * <p>Decoding drops a tag it cannot name rather than refusing the packet, which keeps one bad entry
 * from costing a player their whole filter column.
 */
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
