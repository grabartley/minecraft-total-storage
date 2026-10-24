package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.tag.PresetTagResolver;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class SortCandidateFactory implements SortCandidates<ItemVariant> {
  private final PresetTagResolver presetTags;

  public SortCandidateFactory() {
    this(new PresetTagResolver());
  }

  public SortCandidateFactory(PresetTagResolver presetTags) {
    this.presetTags = Objects.requireNonNull(presetTags, "presetTags");
  }

  @Override
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
