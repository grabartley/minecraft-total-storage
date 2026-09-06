package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalAccessRules;
import com.grahambartley.toomanychests.access.TerminalAccessService;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Objects;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Builds one player's view of one terminal, twice: once for the handler the server keeps and once
 * for the payload the client is sent.
 *
 * <p>Both are computed from the terminal's settings as they stand at that instant rather than
 * cached between the two calls, because the settings are the record and a copy taken a moment
 * earlier is one more thing that can be stale.
 */
public record TerminalScreenFactory(ServerWorld world, BlockPos pos)
    implements ExtendedScreenHandlerFactory<TerminalOpenData> {

  public TerminalScreenFactory {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(pos, "pos");
    pos = pos.toImmutable();
  }

  @Override
  public Text getDisplayName() {
    return ModBlocks.TERMINAL.getName();
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
    return new TerminalScreenHandler(syncId, inventory, openDataFor(player));
  }

  @Override
  public TerminalOpenData getScreenOpeningData(ServerPlayerEntity player) {
    return openDataFor(player);
  }

  private TerminalOpenData openDataFor(PlayerEntity player) {
    final TerminalSettings settings =
        TerminalLookup.settingsAt(world, pos).orElseGet(TerminalSettings::defaults);
    return new TerminalOpenData(
        pos,
        TerminalRadius.effective(
            settings.radius(), ServerConfigService.current().maxTerminalRadius()),
        TerminalPermission.of(
            TerminalAccessRules.mayAdminister(
                settings, player.getUuid(), TerminalAccessService.isOperator(player))),
        settings.tags());
  }
}
