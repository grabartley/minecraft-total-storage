package com.grahambartley.toomanychests.tag;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class PresetTagDefinitions {
  private static final String TAG_PREFIX = "preset/";

  private static final Map<PresetTag, TagKey<Item>> TAGS = createTags();

  private PresetTagDefinitions() {}

  public static Map<PresetTag, TagKey<Item>> all() {
    return TAGS;
  }

  private static Map<PresetTag, TagKey<Item>> createTags() {
    EnumMap<PresetTag, TagKey<Item>> tags = new EnumMap<>(PresetTag.class);
    for (PresetTag preset : PresetTag.all()) {
      if (preset != PresetTag.FALLBACK) {
        tags.put(preset, tagFor(preset));
      }
    }
    return Collections.unmodifiableMap(tags);
  }

  private static TagKey<Item> tagFor(PresetTag preset) {
    return TagKey.of(
        RegistryKeys.ITEM, Identifier.of(TooManyChests.MOD_ID, TAG_PREFIX + preset.id()));
  }
}
