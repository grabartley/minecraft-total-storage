package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Block origins for index tests, which care only that origins are distinct value types. */
final class TestOrigins {
  static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  private TestOrigins() {}

  static StorageOrigin at(int x) {
    return new BlockStorageOrigin(OVERWORLD, new BlockPos(x, 64, 0));
  }

  static StorageOrigin[] chain(int count) {
    StorageOrigin[] origins = new StorageOrigin[count];
    for (int i = 0; i < count; i++) {
      origins[i] = at(i);
    }
    return origins;
  }
}
