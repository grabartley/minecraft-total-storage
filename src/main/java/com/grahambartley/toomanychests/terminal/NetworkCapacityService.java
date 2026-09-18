package com.grahambartley.toomanychests.terminal;

import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkCapacityService {
  private final ContainerRoleService roles;

  public NetworkCapacityService() {
    this(new ContainerRoleService());
  }

  public NetworkCapacityService(ContainerRoleService roles) {
    this.roles = Objects.requireNonNull(roles, "roles");
  }

  public NetworkCapacity measure(ServerWorld world, BlockPos terminal) {
    return NetworkCapacity.of(roles.containers(world, terminal));
  }
}
