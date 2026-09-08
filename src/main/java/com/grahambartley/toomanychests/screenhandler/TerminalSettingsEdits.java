package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.network.ContainerRoleEditPayload;
import com.grahambartley.toomanychests.network.TerminalAccessEditPayload;
import com.grahambartley.toomanychests.network.TerminalActionPayload;
import com.grahambartley.toomanychests.network.TerminalRadiusEditPayload;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalRadiusService;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Applies what a settings panel asked to change, and is the only path those requests may take.
 *
 * <p>Every edit is checked against the terminal rather than against the panel that sent it. Per ADR
 * 0008 a client claiming it may administer gains nothing here: the flag it was sent decides what
 * was drawn, and this decides what happens.
 *
 * <p>Every edit ends by pushing a fresh panel to everyone looking at the terminal, not only to the
 * player who made it. Two people configuring one base is the case ADR 0025 already forces the mod
 * to handle for the item listing, and a role changed under someone else's cursor is the same
 * problem.
 */
public final class TerminalSettingsEdits {
  private final TerminalSettingsService panels;
  private final ContainerRoleService roles;
  private final TerminalRadiusService radius;

  public TerminalSettingsEdits(TerminalSettingsService panels) {
    this(panels, new ContainerRoleService(), new TerminalRadiusService(TerminalNetworks.power()));
  }

  public TerminalSettingsEdits(
      TerminalSettingsService panels, ContainerRoleService roles, TerminalRadiusService radius) {
    this.panels = Objects.requireNonNull(panels, "panels");
    this.roles = Objects.requireNonNull(roles, "roles");
    this.radius = Objects.requireNonNull(radius, "radius");
  }

  public boolean setRadius(ServerPlayerEntity player, TerminalRadiusEditPayload payload) {
    final TerminalOperationContext context =
        administrable(player, payload == null ? 0 : payload.syncId());
    if (context == null) {
      return false;
    }
    final Optional<TerminalRadius> applied =
        radius.apply(context.world(), context.terminal(), payload.blocks());
    if (applied.isEmpty()) {
      return false;
    }
    if (applied.get().clamped()) {
      player.sendMessage(TerminalMessages.radiusClamped(applied.get().blocks()), true);
    }
    ContainerHighlightService.highlight(player, context.world(), context.terminal());
    return pushed(context);
  }

  public boolean setAccessMode(ServerPlayerEntity player, TerminalAccessEditPayload payload) {
    final TerminalOperationContext context =
        administrable(player, payload == null ? 0 : payload.syncId());
    if (context == null) {
      return false;
    }
    TerminalAccessControl.trust()
        .setAccessMode(context.world(), context.terminal(), payload.mode());
    return pushed(context);
  }

  public boolean setRole(ServerPlayerEntity player, ContainerRoleEditPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    final RoleChangeOutcome outcome =
        roles.assign(
            context.world(),
            context.terminal(),
            TerminalCaller.of(player),
            payload.container(),
            payload.role());
    if (outcome == RoleChangeOutcome.DENIED) {
      player.sendMessage(TerminalMessages.notYours(), true);
      panels.refresh(player);
      return false;
    }
    if (outcome == RoleChangeOutcome.NOT_IN_NETWORK) {
      player.sendMessage(TerminalMessages.containerNotInNetwork(), true);
      panels.refresh(player);
      return false;
    }
    return pushed(context);
  }

  /**
   * A highlight is open to anyone who may use the terminal, because refusing a player the answer to
   * "which chests is this reaching" would make the radius harder to understand rather than the base
   * harder to reach. A rescan is not: per ADR 0007 it tears the network down and builds it again,
   * which is both work and a change to what everyone else at the terminal is looking at.
   *
   * <p>The outlines are drawn after the rescan rather than before it. Showing them first would
   * describe the network the walk was about to replace, which is exactly the case a player asks for
   * a rescan in.
   */
  public boolean act(ServerPlayerEntity player, TerminalActionPayload payload) {
    if (payload == null) {
      return false;
    }
    if (payload.action() != TerminalAction.RESCAN) {
      final TerminalOperationContext context =
          TerminalOperationGate.open(player, payload.syncId()).orElse(null);
      if (context == null) {
        return false;
      }
      ContainerHighlightService.highlight(player, context.world(), context.terminal());
      return true;
    }
    final TerminalOperationContext context = administrable(player, payload.syncId());
    if (context == null) {
      return false;
    }
    TerminalNetworks.power().rescan(context.world(), context.terminal());
    ContainerHighlightService.highlight(player, context.world(), context.terminal());
    return pushed(context);
  }

  /** Everyone looking at this terminal is shown the change, not only whoever made it. */
  private boolean pushed(TerminalOperationContext context) {
    final TerminalAddress terminal = TerminalAddress.of(context.world(), context.terminal());
    TerminalViewerSync.playersOn(context.world().getServer(), terminal).forEach(panels::refresh);
    return true;
  }

  /**
   * The screen, resolved and checked for the permission every setting needs. A player who may use
   * the terminal but not configure it is told so rather than ignored, because a control that does
   * nothing is indistinguishable from a broken one.
   */
  private TerminalOperationContext administrable(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, syncId).orElse(null);
    if (context == null) {
      return null;
    }
    if (mayAdminister(context.world(), context.terminal(), player)) {
      return context;
    }
    player.sendMessage(TerminalMessages.notYours(), true);
    panels.refresh(player);
    return null;
  }

  private static boolean mayAdminister(
      ServerWorld world, BlockPos terminal, ServerPlayerEntity player) {
    return TerminalLookup.settingsAt(world, terminal)
        .map(TerminalCaller.of(player)::mayAdminister)
        .orElse(false);
  }
}
