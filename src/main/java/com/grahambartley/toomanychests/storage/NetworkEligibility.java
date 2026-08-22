package com.grahambartley.toomanychests.storage;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Answers whether the container at a position may join a storage network. Read fresh on every call
 * rather than cached, so an operator's change takes effect on the next scan without a restart.
 */
public final class NetworkEligibility {
  private final StorageProviderResolver resolver;
  private final Supplier<TooManyChestsConfig> config;

  public NetworkEligibility() {
    this(new StorageProviderResolver(), ServerConfigService::current);
  }

  public NetworkEligibility(
      StorageProviderResolver resolver, Supplier<TooManyChestsConfig> config) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.config = Objects.requireNonNull(config, "config");
  }

  public boolean isEligible(World world, BlockPos pos) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(pos, "pos");
    final BlockState state = world.getBlockState(pos);
    return NetworkEligibilityRules.isEligible(
        resolver.resolve(world, pos).isPresent(),
        state.isIn(NetworkBlockTags.NETWORK_INELIGIBLE),
        config.get().containerEligibilityOverrideFor(Registries.BLOCK.getId(state.getBlock())));
  }
}
