package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import java.util.List;

/**
 * Everything the settings panel draws, as one answer built on the server.
 *
 * <p>Assembled server-side for the same reason the item listing is: the client holds none of the
 * facts. It does not know the server's radius cap, it cannot resolve a trust list of UUIDs into
 * names, and per ADR 0004 the roles live on a block entity it never sees. A panel that guessed at
 * any of those would be a panel that disagrees with the terminal it is drawing.
 *
 * <p>{@code mayAdminister} decides what is drawn as usable and nothing more. Every edit is checked
 * again when it arrives, so a modified client that flips this gains buttons that refuse it.
 *
 * <p>The container list is a page rather than the whole set, because a network is allowed to reach
 * far more containers than a panel could draw or a packet should carry.
 *
 * @param radius what the terminal is configured for, which it keeps whatever the cap does
 * @param effectiveRadius how far it really reaches once the server cap is applied
 * @param maxRadius the server cap, so the limit is never mysterious
 * @param trusted the trust list already resolved to names, empty for a caller who may not read it
 * @param tally how many containers hold each role, across the whole network rather than this page
 * @param containerOffset how many containers come before the first row here
 * @param containerTotal how many containers the network holds in total
 * @param containers the rows themselves, nearest to the terminal first
 */
public record NetworkSettingsView(
    int radius,
    int effectiveRadius,
    int maxRadius,
    TerminalAccessMode accessMode,
    boolean mayAdminister,
    boolean powered,
    List<String> trusted,
    RoleTally tally,
    int containerOffset,
    int containerTotal,
    List<ContainerRoleEntry> containers) {

  /** A ceiling on the rows one answer may carry, well past what the panel can show at once. */
  public static final int MAX_CONTAINERS = 64;

  /** A ceiling on the trust list one answer may carry. */
  public static final int MAX_TRUSTED = 64;

  public NetworkSettingsView {
    accessMode = accessMode == null ? TerminalAccessMode.OWNER : accessMode;
    trusted = trusted == null ? List.of() : List.copyOf(trusted);
    tally = tally == null ? RoleTally.EMPTY : tally;
    containers = containers == null ? List.of() : List.copyOf(containers);
    if (trusted.size() > MAX_TRUSTED) {
      throw new IllegalArgumentException("a view may not carry " + trusted.size() + " trusted");
    }
    if (containers.size() > MAX_CONTAINERS) {
      throw new IllegalArgumentException(
          "a view may not carry " + containers.size() + " containers");
    }
    containerOffset = Math.max(0, containerOffset);
    containerTotal = Math.max(0, containerTotal);
  }

  /** What a screen holds before anything has answered it. */
  public static NetworkSettingsView pending() {
    return new NetworkSettingsView(
        TerminalSettings.DEFAULT_RADIUS,
        TerminalSettings.DEFAULT_RADIUS,
        TerminalSettings.MAX_RADIUS,
        TerminalAccessMode.OWNER,
        false,
        false,
        List.of(),
        RoleTally.EMPTY,
        0,
        0,
        List.of());
  }

  /** The smallest reach a terminal may be set to, so a stepper at the end greys out. */
  public int minRadius() {
    return TerminalSettings.MIN_RADIUS;
  }

  /** Whether the terminal is running at less than it was configured for, which is worth saying. */
  public boolean isClamped() {
    return effectiveRadius < radius;
  }

  /** The row at a position in the whole list, or null when this page does not cover it. */
  public ContainerRoleEntry at(int index) {
    final int within = index - containerOffset;
    return within < 0 || within >= containers.size() ? null : containers.get(within);
  }

  public boolean hasContainers() {
    return containerTotal > 0;
  }

  @Override
  public String toString() {
    return "NetworkSettingsView[radius="
        + radius
        + "/"
        + effectiveRadius
        + " of "
        + maxRadius
        + ", access="
        + accessMode
        + ", containers="
        + containerTotal
        + "]";
  }
}
