package com.grahambartley.totalstorage.access;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class TerminalAccessControl {
  private static final TerminalViewers VIEWERS = new TerminalViewers();
  private static final TerminalAccessService ACCESS = new TerminalAccessService(VIEWERS);
  private static final TerminalTrustService TRUST = new TerminalTrustService(ACCESS);

  private TerminalAccessControl() {}

  public static TerminalViewers viewers() {
    return VIEWERS;
  }

  public static TerminalAccessService access() {
    return ACCESS;
  }

  public static TerminalTrustService trust() {
    return TRUST;
  }

  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> VIEWERS.closedEverything(handler.getPlayer().getUuid()));
  }
}
