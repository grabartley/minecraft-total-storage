package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import java.util.List;

public record NetworkSettingsView(
    boolean answered,
    int radius,
    int effectiveRadius,
    int maxRadius,
    TerminalAccessMode accessMode,
    boolean mayAdminister,
    boolean powered,
    List<String> trusted,
    NetworkCapacity capacity,
    int containerOffset,
    int containerTotal,
    List<ContainerRoleEntry> containers) {

  public static final int MAX_CONTAINERS = 64;

  public static final int MAX_TRUSTED = 64;

  private static final NetworkSettingsView PENDING =
      new NetworkSettingsView(
          false,
          TerminalSettings.DEFAULT_RADIUS,
          TerminalSettings.DEFAULT_RADIUS,
          TerminalSettings.MAX_RADIUS,
          TerminalAccessMode.OWNER,
          false,
          false,
          List.of(),
          NetworkCapacity.EMPTY,
          0,
          0,
          List.of());

  public NetworkSettingsView(
      int radius,
      int effectiveRadius,
      int maxRadius,
      TerminalAccessMode accessMode,
      boolean mayAdminister,
      boolean powered,
      List<String> trusted,
      NetworkCapacity capacity,
      int containerOffset,
      int containerTotal,
      List<ContainerRoleEntry> containers) {
    this(
        true,
        radius,
        effectiveRadius,
        maxRadius,
        accessMode,
        mayAdminister,
        powered,
        trusted,
        capacity,
        containerOffset,
        containerTotal,
        containers);
  }

  public NetworkSettingsView {
    accessMode = accessMode == null ? TerminalAccessMode.OWNER : accessMode;
    trusted = trusted == null ? List.of() : List.copyOf(trusted);
    capacity = capacity == null ? NetworkCapacity.EMPTY : capacity;
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

  public static NetworkSettingsView pending() {
    return PENDING;
  }

  public int minRadius() {
    return TerminalSettings.MIN_RADIUS;
  }

  public boolean isClamped() {
    return effectiveRadius < radius;
  }

  public ContainerRoleEntry at(int index) {
    final int within = index - containerOffset;
    return within < 0 || within >= containers.size() ? null : containers.get(within);
  }

  public boolean hasContainers() {
    return containerTotal > 0;
  }

  @Override
  public String toString() {
    return "NetworkSettingsView[answered="
        + answered
        + ", radius="
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
