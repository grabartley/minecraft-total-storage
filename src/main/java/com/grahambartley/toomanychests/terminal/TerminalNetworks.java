package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;

/** The one registry of running networks, and the switch that starts and stops them. */
public final class TerminalNetworks {
  private static final TerminalNetworkRegistry<NetworkMembership> REGISTRY =
      new TerminalNetworkRegistry<>();
  private static final TerminalPower POWER = new TerminalPower(REGISTRY);

  private TerminalNetworks() {}

  public static TerminalNetworkRegistry<NetworkMembership> registry() {
    return REGISTRY;
  }

  public static TerminalPower power() {
    return POWER;
  }
}
