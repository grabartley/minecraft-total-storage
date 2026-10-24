package com.grahambartley.totalstorage.index;

import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.Registries;

public final class ItemVariantSortKey implements Function<ItemVariant, String> {
  @Override
  public String apply(ItemVariant variant) {
    return Registries.ITEM.getId(variant.getItem()).toString();
  }
}
