package com.grahambartley.toomanychests.index;

import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.Registries;

/**
 * Orders variants by registry identifier, per ADR 0005, so storage does not rearrange itself when a
 * player changes their game language.
 */
public final class ItemVariantSortKey implements Function<ItemVariant, String> {
  @Override
  public String apply(ItemVariant variant) {
    return Registries.ITEM.getId(variant.getItem()).toString();
  }
}
