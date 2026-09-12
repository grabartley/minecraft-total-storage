package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.access.TerminalAdmin;
import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.network.ContainerRoleEditPayload;
import com.grahambartley.toomanychests.network.TerminalAccessEditPayload;
import com.grahambartley.toomanychests.network.TerminalActionPayload;
import com.grahambartley.toomanychests.network.TerminalRadiusEditPayload;
import com.grahambartley.toomanychests.operation.ExpensiveOperation;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalPower;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalRadiusService;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

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
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context = administrable(player, payload.syncId());
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
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context = administrable(player, payload.syncId());
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
    if (outcome == RoleChangeOutcome.NO_TERMINAL) {
      panels.refresh(player);
      return false;
    }
    return pushed(context);
  }

  public boolean act(ServerPlayerEntity player, TerminalActionPayload payload) {
    if (payload == null) {
      return false;
    }
    if (payload.action() == TerminalAction.HIGHLIGHT) {
      final TerminalOperationContext context =
          TerminalOperationGate.open(player, payload.syncId()).orElse(null);
      if (context == null) {
        return false;
      }
      ContainerHighlightService.highlight(player, context.world(), context.terminal());
      return true;
    }
    if (payload.action() != TerminalAction.RESCAN) {
      return false;
    }
    final TerminalOperationContext context = administrable(player, payload.syncId());
    if (context == null) {
      return false;
    }
    if (!OperationGuards.allows(player, ExpensiveOperation.RESCAN)) {
      return false;
    }
    final TerminalPower power = TerminalNetworks.power();
    power.rescan(context.world(), context.terminal());
    player.sendMessage(
        TerminalMessages.rescanned(power.containerCount(context.world(), context.terminal())),
        true);
    ContainerHighlightService.highlight(player, context.world(), context.terminal());
    return pushed(context);
  }

  private boolean pushed(TerminalOperationContext context) {
    final TerminalAddress terminal = TerminalAddress.of(context.world(), context.terminal());
    TerminalViewerSync.playersOn(context.world().getServer(), terminal).forEach(panels::refresh);
    return true;
  }

  private TerminalOperationContext administrable(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, syncId).orElse(null);
    if (context == null) {
      return null;
    }
    if (TerminalAdmin.mayAdminister(context.world(), context.terminal(), player)) {
      return context;
    }
    player.sendMessage(TerminalMessages.notYours(), true);
    panels.refresh(player);
    return null;
  }
}
