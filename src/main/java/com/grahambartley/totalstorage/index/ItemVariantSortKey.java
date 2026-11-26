package com.grahambartley.totalstorage.index;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public final class ItemVariantSortKey implements Function<ItemVariant, String> {
  private final Map<Item, String> keys = new IdentityHashMap<>();

  @Override
  public String apply(ItemVariant variant) {
    return keys.computeIfAbsent(variant.getItem(), item -> Registries.ITEM.getId(item).toString());
  }
}
