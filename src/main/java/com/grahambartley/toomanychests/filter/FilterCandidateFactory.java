package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.tag.PresetTagResolver;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/**
 * Flattens an item variant into the plain data the filter engine matches against.
 *
 * <p>Per ADR 0012 this is the only class in the filter package that names {@code ItemVariant}. It
 * holds no matching logic of its own, so a mistake here is a mistake in reading the registry rather
 * than in the rule, and it is covered by a gametest instead of a unit test.
 */
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
