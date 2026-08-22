package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class NetworkBlockTags {
  /**
   * Blocks a storage network must never treat as storage, even though they expose item storage.
   * Data-driven so a pack can add or remove entries without a code change.
   */
  public static final TagKey<Block> NETWORK_INELIGIBLE =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(TooManyChests.MOD_ID, "network_ineligible"));

  private NetworkBlockTags() {}
}
