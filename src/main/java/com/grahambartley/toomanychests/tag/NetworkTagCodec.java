package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

public final class NetworkTagCodec {
  private record StoredTag(String id, String displayName, List<String> items) {
    static final Codec<StoredTag> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("id").forGetter(StoredTag::id),
                        Codec.STRING.optionalFieldOf("name", "").forGetter(StoredTag::displayName),
                        Codec.STRING
                            .listOf()
                            .optionalFieldOf("items", List.of())
                            .forGetter(StoredTag::items))
                    .apply(instance, StoredTag::new));
  }

  public static final Codec<NetworkTagSet> CODEC =
      StoredTag.CODEC.listOf().xmap(NetworkTagCodec::assemble, NetworkTagCodec::disassemble);

  private NetworkTagCodec() {}

  private static NetworkTagSet assemble(List<StoredTag> stored) {
    final List<NetworkTag> tags = new ArrayList<>(stored.size());
    for (final StoredTag entry : stored) {
      if (!FilterText.isBlank(entry.id())) {
        tags.add(NetworkTag.custom(entry.id(), entry.displayName(), entry.items()));
      }
    }
    return new NetworkTagSet(tags);
  }

  private static List<StoredTag> disassemble(NetworkTagSet tags) {
    return tags.custom().stream()
        .map(tag -> new StoredTag(tag.id(), tag.displayName(), List.copyOf(tag.items())))
        .toList();
  }
}
