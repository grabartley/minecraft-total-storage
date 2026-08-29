package com.grahambartley.toomanychests;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

  public static final Item TERMINAL =
      Registry.register(
          Registries.ITEM,
          Identifier.of(TooManyChests.MOD_ID, "terminal"),
          new BlockItem(ModBlocks.TERMINAL, new Item.Settings()));

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(TERMINAL));
  }
}
