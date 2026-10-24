package com.grahambartley.totalstorage;

import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
  public static final BlockEntityType<TerminalBlockEntity> TERMINAL =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(TotalStorage.MOD_ID, "terminal"),
          BlockEntityType.Builder.create(TerminalBlockEntity::new, ModBlocks.TERMINAL).build());

  public static void initialize() {}
}
