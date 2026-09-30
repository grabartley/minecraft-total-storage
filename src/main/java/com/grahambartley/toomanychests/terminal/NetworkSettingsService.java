package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalAccessRules;
import com.grahambartley.toomanychests.access.TerminalAccessService;
import com.grahambartley.toomanychests.access.TrustedPlayerNames;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.network.PayloadCapLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class NetworkSettingsService {
  private final ContainerRoleService roles;

  public NetworkSettingsService() {
    this(new ContainerRoleService());
  }

  public NetworkSettingsService(ContainerRoleService roles) {
    this.roles = Objects.requireNonNull(roles, "roles");
  }

  public NetworkSettingsView viewFor(
      ServerPlayerEntity player, BlockPos terminal, ListingWindow page) {
    Objects.requireNonNull(terminal, "terminal");
    if (player == null || !(player.getWorld() instanceof ServerWorld world)) {
      return NetworkSettingsView.pending();
    }
    final TerminalSettings settings = TerminalLookup.settingsAt(world, terminal).orElse(null);
    if (settings == null) {
      return NetworkSettingsView.pending();
    }
    final boolean mayAdminister =
        TerminalAccessRules.mayAdminister(
            settings, player.getUuid(), TerminalAccessService.isOperator(player));
    final List<ContainerRoleEntry> all = roles.containers(world, terminal);
    final ListingWindow window = page == null ? ListingWindow.EMPTY : page;
    final int maximum = ServerConfigService.current().maxTerminalRadius();
    return NetworkSettingsView.answered(
        settings.radius(),
        TerminalRadius.effective(settings.radius(), maximum),
        maximum,
        settings.accessMode(),
        mayAdminister,
        isPowered(world, terminal),
        mayAdminister ? namesOf(world.getServer(), settings.trusted()) : List.of(),
        NetworkCapacity.of(all),
        window.offset(),
        all.size(),
        window.sliceOf(all));
  }

  private static boolean isPowered(ServerWorld world, BlockPos terminal) {
    final BlockState state = world.getBlockState(terminal);
    return state.contains(TerminalBlock.POWERED) && state.get(TerminalBlock.POWERED);
  }

  private static List<String> namesOf(MinecraftServer server, Set<UUID> trusted) {
    final List<String> names = new ArrayList<>(trusted.size());
    for (final UUID player : trusted) {
      if (names.size() >= NetworkSettingsView.MAX_TRUSTED) {
        PayloadCapLog.reached("trusted players", NetworkSettingsView.MAX_TRUSTED, trusted.size());
        break;
      }
      names.add(TrustedPlayerNames.nameOf(server, player));
    }
    return List.copyOf(names);
  }
}
