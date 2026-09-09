package com.grahambartley.toomanychests.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public final class PresetTagResolver {
  public Set<PresetTag> resolve(Item item) {
    RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
    List<PresetTag> matched = new ArrayList<>();
    for (Map.Entry<PresetTag, TagKey<Item>> preset : PresetTagDefinitions.all().entrySet()) {
      if (entry.isIn(preset.getValue())) {
        matched.add(preset.getKey());
      }
    }
    if (item.getComponents().contains(DataComponentTypes.FOOD)) {
      matched.add(PresetTag.FOOD);
    }
    return PresetTagRules.assign(matched);
  }
}
