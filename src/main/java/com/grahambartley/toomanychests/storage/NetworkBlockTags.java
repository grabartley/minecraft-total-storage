package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class NetworkBlockTags {

  public static final TagKey<Block> NETWORK_INELIGIBLE =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(TooManyChests.MOD_ID, "network_ineligible"));

  private NetworkBlockTags() {}
}
