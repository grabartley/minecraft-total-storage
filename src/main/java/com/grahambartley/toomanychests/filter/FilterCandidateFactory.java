package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.tag.PresetTagResolver;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class FilterCandidateFactory {
  private final PresetTagResolver presetTags;

  public FilterCandidateFactory() {
    this(new PresetTagResolver());
  }

  public FilterCandidateFactory(PresetTagResolver presetTags) {
    this.presetTags = Objects.requireNonNull(presetTags, "presetTags");
  }

  public FilterCandidate of(ItemVariant variant) {
    Objects.requireNonNull(variant, "variant");
    final ItemStack stack = variant.toStack();
    final Set<String> tags = new LinkedHashSet<>();
    for (final PresetTag preset : presetTags.resolve(variant.getItem())) {
      tags.add(preset.id());
    }
    return new FilterCandidate(
        Registries.ITEM.getId(variant.getItem()).toString(), stack.getName().getString(), tags);
  }
}
