package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.block.TerminalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

  public static final Block TERMINAL =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(TooManyChests.MOD_ID, "terminal"),
          new TerminalBlock(
              AbstractBlock.Settings.create()
                  .mapColor(MapColor.GRAY)
                  .strength(3.5f)
                  .requiresTool()
                  .sounds(BlockSoundGroup.METAL)));

  public static void initialize() {}
}
