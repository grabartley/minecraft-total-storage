package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.access.TerminalAccessRules;
import com.grahambartley.toomanychests.access.TerminalAccessService;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the settings panel's answer from the terminal as it stands right now.
 *
 * <p>Read fresh on every request rather than cached, for the same reason {@link
 * com.grahambartley.toomanychests.access.TerminalAccessService} reads settings fresh: a trust list
 * edited a tick ago has to bind the panel someone else is looking at, and a cache is exactly how
 * two players end up disagreeing about who owns a base.
 *
 * <p>The trust list is only assembled for a caller entitled to read it. It names players who are
 * not the one looking, and handing it to everyone who can open an open terminal would be a leak
 * dressed up as a feature.
 */
public final class NetworkSettingsService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkSettingsService.class);

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
    return new NetworkSettingsView(
        settings.radius(),
        TerminalRadius.effective(settings.radius(), maximum),
        maximum,
        settings.accessMode(),
        mayAdminister,
        isPowered(world, terminal),
        mayAdminister ? namesOf(world.getServer(), settings.trusted()) : List.of(),
        RoleTally.of(all.stream().map(ContainerRoleEntry::role).toList()),
        window.offset(),
        all.size(),
        window.sliceOf(all));
  }

  private static boolean isPowered(ServerWorld world, BlockPos terminal) {
    final net.minecraft.block.BlockState state = world.getBlockState(terminal);
    return state.contains(TerminalBlock.POWERED) && state.get(TerminalBlock.POWERED);
  }

  /**
   * Trust list UUIDs turned into names, which only the server can do. A player who has never been
   * seen by this server keeps their UUID rather than vanishing from the list, because a name the
   * cache cannot supply is still an entry the owner needs to be able to remove.
   */
  private static List<String> namesOf(MinecraftServer server, java.util.Set<UUID> trusted) {
    final List<String> names = new ArrayList<>(trusted.size());
    for (final UUID player : trusted) {
      if (names.size() >= NetworkSettingsView.MAX_TRUSTED) {
        LOGGER.info(
            "Showing the first {} of {} trusted players",
            NetworkSettingsView.MAX_TRUSTED,
            trusted.size());
        break;
      }
      names.add(nameOf(server, player));
    }
    return List.copyOf(names);
  }

  private static String nameOf(MinecraftServer server, UUID player) {
    if (server == null || server.getUserCache() == null) {
      return player.toString();
    }
    return server
        .getUserCache()
        .getByUuid(player)
        .map(GameProfile::getName)
        .orElseGet(player::toString);
  }
}
