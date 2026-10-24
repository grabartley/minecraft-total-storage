package com.grahambartley.totalstorage.tag;

import com.grahambartley.totalstorage.filter.FilterText;
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
      StoredTag.CODEC
          .listOf()
          .xmap(stored -> new NetworkTagSet(assemble(stored).tags()), NetworkTagCodec::disassemble);

  public static final Codec<TagSetContents> CONTENTS_CODEC =
      StoredTag.CODEC.listOf().xmap(NetworkTagCodec::assemble, contents -> store(contents.tags()));

  private NetworkTagCodec() {}

  private static TagSetContents assemble(List<StoredTag> stored) {
    final List<NetworkTag> tags = new ArrayList<>(stored.size());
    int malformed = 0;
    for (final StoredTag entry : stored) {
      if (FilterText.isBlank(entry.id())) {
        malformed++;
      } else {
        tags.add(NetworkTag.custom(entry.id(), entry.displayName(), entry.items()));
      }
    }
    return new TagSetContents(tags, malformed);
  }

  private static List<StoredTag> disassemble(NetworkTagSet tags) {
    return store(tags.custom());
  }

  private static List<StoredTag> store(List<NetworkTag> tags) {
    return tags.stream()
        .map(tag -> new StoredTag(tag.id(), tag.displayName(), List.copyOf(tag.items())))
        .toList();
  }
}
