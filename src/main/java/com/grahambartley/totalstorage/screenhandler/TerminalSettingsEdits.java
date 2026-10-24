package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.access.TerminalAdmin;
import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.access.TerminalTrustService;
import com.grahambartley.totalstorage.access.TrustEditOutcome;
import com.grahambartley.totalstorage.network.ContainerFilterEditPayload;
import com.grahambartley.totalstorage.network.ContainerNameEditPayload;
import com.grahambartley.totalstorage.network.ContainerRoleEditPayload;
import com.grahambartley.totalstorage.network.TerminalAccessEditPayload;
import com.grahambartley.totalstorage.network.TerminalActionPayload;
import com.grahambartley.totalstorage.network.TerminalRadiusEditPayload;
import com.grahambartley.totalstorage.network.TerminalTrustEditPayload;
import com.grahambartley.totalstorage.network.TerminalTrustReplyPayload;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.operation.TerminalNotice;
import com.grahambartley.totalstorage.operation.TerminalOperationContext;
import com.grahambartley.totalstorage.operation.TerminalOperationGate;
import com.grahambartley.totalstorage.terminal.ContainerFilterService;
import com.grahambartley.totalstorage.terminal.ContainerHighlightService;
import com.grahambartley.totalstorage.terminal.ContainerNamingService;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.RoleChangeOutcome;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalMessages;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import com.grahambartley.totalstorage.terminal.TerminalPower;
import com.grahambartley.totalstorage.terminal.TerminalRadius;
import com.grahambartley.totalstorage.terminal.TerminalRadiusService;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalSettingsEdits {
  private final TerminalSettingsService panels;
  private final ContainerRoleService roles;
  private final TerminalRadiusService radius;
  private final ContainerNamingService names;
  private final ContainerFilterService filters;

  public TerminalSettingsEdits(TerminalSettingsService panels) {
    this(panels, new ContainerRoleService(), new TerminalRadiusService(TerminalNetworks.power()));
  }

  public TerminalSettingsEdits(
      TerminalSettingsService panels, ContainerRoleService roles, TerminalRadiusService radius) {
    this(panels, roles, radius, new ContainerNamingService(), new ContainerFilterService());
  }

  public TerminalSettingsEdits(
      TerminalSettingsService panels,
      ContainerRoleService roles,
      TerminalRadiusService radius,
      ContainerNamingService names,
      ContainerFilterService filters) {
    this.panels = Objects.requireNonNull(panels, "panels");
    this.roles = Objects.requireNonNull(roles, "roles");
    this.radius = Objects.requireNonNull(radius, "radius");
    this.names = Objects.requireNonNull(names, "names");
    this.filters = Objects.requireNonNull(filters, "filters");
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
      TerminalNotice.tell(player, TerminalMessages.radiusClamped(applied.get().blocks()));
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

  public TrustEditOutcome setTrust(ServerPlayerEntity player, TerminalTrustEditPayload payload) {
    if (payload == null) {
      return TrustEditOutcome.DENIED;
    }
    final TerminalOperationContext context = administrable(player, payload.syncId());
    final TrustEditOutcome outcome =
        context == null ? TrustEditOutcome.DENIED : trustEdit(context, payload);
    if (ServerPlayNetworking.canSend(player, TerminalTrustReplyPayload.ID)) {
      ServerPlayNetworking.send(
          player, new TerminalTrustReplyPayload(payload.syncId(), payload.player(), outcome));
    }
    if (outcome.changedTheList()) {
      pushed(context);
    }
    return outcome;
  }

  private static TrustEditOutcome trustEdit(
      TerminalOperationContext context, TerminalTrustEditPayload payload) {
    final TerminalTrustService trust = TerminalAccessControl.trust();
    return payload.trusted()
        ? trust.trustNamed(context.world(), context.terminal(), payload.player())
        : trust.untrustNamed(context.world(), context.terminal(), payload.player());
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
    return applied(
        player,
        context,
        roles.assign(
            context.world(),
            context.terminal(),
            TerminalCaller.of(player),
            payload.container(),
            payload.role()));
  }

  public boolean setName(ServerPlayerEntity player, ContainerNameEditPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    return applied(
        player,
        context,
        names.rename(
            context.world(),
            context.terminal(),
            TerminalCaller.of(player),
            payload.container(),
            payload.name()));
  }

  public boolean setFilterTag(ServerPlayerEntity player, ContainerFilterEditPayload payload) {
    if (payload == null) {
      return false;
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    return applied(
        player,
        context,
        filters.accept(
            context.world(),
            context.terminal(),
            TerminalCaller.of(player),
            payload.container(),
            payload.tagId(),
            payload.accepted()));
  }

  private boolean applied(
      ServerPlayerEntity player, TerminalOperationContext context, RoleChangeOutcome outcome) {
    if (outcome == RoleChangeOutcome.DENIED) {
      TerminalNotice.tell(player, TerminalMessages.notYours());
      panels.refresh(player);
      return false;
    }
    if (outcome == RoleChangeOutcome.NOT_IN_NETWORK) {
      TerminalNotice.tell(player, TerminalMessages.containerNotInNetwork());
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
    return switch (payload.action()) {
      case HIGHLIGHT -> highlight(player, payload.syncId());
      case RESCAN -> rescan(player, payload.syncId());
      case SORT_NETWORK, CONSOLIDATE -> false;
    };
  }

  private boolean highlight(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, syncId).orElse(null);
    if (context == null) {
      return false;
    }
    TerminalNotice.tell(
        player,
        TerminalMessages.highlighted(
            ContainerHighlightService.highlight(player, context.world(), context.terminal())));
    return true;
  }

  private boolean rescan(ServerPlayerEntity player, int syncId) {
    final TerminalOperationContext context = administrable(player, syncId);
    if (context == null) {
      return false;
    }
    if (!OperationGuards.allows(player, ExpensiveOperation.RESCAN)) {
      return false;
    }
    final TerminalPower power = TerminalNetworks.power();
    power.rescan(context.world(), context.terminal());
    TerminalNotice.tell(
        player,
        TerminalMessages.rescanned(power.containerCount(context.world(), context.terminal())));
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
    TerminalNotice.tell(player, TerminalMessages.notYours());
    panels.refresh(player);
    return null;
  }
}
