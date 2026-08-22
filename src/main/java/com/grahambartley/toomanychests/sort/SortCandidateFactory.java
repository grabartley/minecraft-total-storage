package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.tag.PresetTagResolver;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/**
 * Flattens an item variant and its count into the plain data the comparator sorts.
 *
 * <p>Per ADR 0012 this is the only class in the sort package that names {@code ItemVariant}. It
 * holds no ordering logic of its own, so a mistake here is a mistake in reading the registry rather
 * than in the rule, and it is covered by a gametest instead of a unit test.
 */
public final class SortCandidateFactory {
  private final PresetTagResolver presetTags;

  public SortCandidateFactory() {
    this(new PresetTagResolver());
  }

  public SortCandidateFactory(PresetTagResolver presetTags) {
    this.presetTags = Objects.requireNonNull(presetTags, "presetTags");
  }

  public SortCandidate of(ItemVariant variant, long count) {
    Objects.requireNonNull(variant, "variant");
    final ItemStack stack = variant.toStack();
    return new SortCandidate(
        presetTags.resolve(variant.getItem()),
        Registries.ITEM.getId(variant.getItem()).toString(),
        DataComponentKey.of(variant.getComponents()),
        stack.getName().getString(),
        count,
        stack.getMaxCount());
  }
}
