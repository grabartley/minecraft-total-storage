package com.grahambartley.toomanychests.access;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** The one gate every terminal operation is checked against, and the list of who has one open. */
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

  /**
   * A player who logs out closes every screen they had open without any screen saying so, and a
   * viewer entry left behind would outlive the session that made it.
   */
  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> VIEWERS.closedEverything(handler.getPlayer().getUuid()));
  }
}
