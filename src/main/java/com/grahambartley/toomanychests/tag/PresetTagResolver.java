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

/**
 * Files an item under every preset it matches. Membership is read live rather than snapshotted, so
 * a datapack reload reclassifies items with no migration.
 *
 * <p>Food is the one preset that also consults a data component, because an edible item added by
 * another mod carries a food component whether or not anyone remembered to tag it.
 */
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
