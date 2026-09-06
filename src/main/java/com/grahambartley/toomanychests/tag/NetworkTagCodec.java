package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.filter.FilterText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists a network's tag set.
 *
 * <p>Per ADR 0023 only custom tags are written. Presets are compiled in and re-seeded on read, so a
 * save carries what the player actually authored and stays correct across a build that adds one.
 *
 * <p>Decoding routes through a plain record first and drops entries it cannot make a tag out of,
 * because throwing here would cost the terminal every other setting rather than one bad tag.
 */
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
