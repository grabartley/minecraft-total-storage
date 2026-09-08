package com.grahambartley.toomanychests.render;

import com.grahambartley.toomanychests.terminal.HighlightedContainer;
import java.util.List;

/**
 * The outlines this client is currently drawing, and how much longer for.
 *
 * <p>Held as a countdown rather than a timestamp so it stops with the game. A highlight measured in
 * wall-clock seconds would keep expiring while a single player world is paused, and a player who
 * opened a menu to think would come back to nothing.
 *
 * <p>Per ADR 0009's spirit the positions are the server's answer rather than this client's guess: a
 * client cannot know a terminal's radius, its eligibility overrides, or which chunks are loaded, so
 * anything it worked out for itself would be confidently wrong.
 */
public final class ContainerHighlights {
  private static List<HighlightedContainer> containers = List.of();
  private static int ticksLeft;

  private ContainerHighlights() {}

  /** Replaces whatever was showing, because a new answer supersedes an older one entirely. */
  public static void show(List<HighlightedContainer> shown, int ticks) {
    containers = shown == null ? List.of() : List.copyOf(shown);
    ticksLeft = containers.isEmpty() ? 0 : Math.max(0, ticks);
  }

  public static void tick() {
    if (ticksLeft > 0 && --ticksLeft == 0) {
      containers = List.of();
    }
  }

  public static List<HighlightedContainer> showing() {
    return ticksLeft > 0 ? containers : List.of();
  }

  public static boolean isShowing() {
    return ticksLeft > 0 && !containers.isEmpty();
  }

  public static void clear() {
    containers = List.of();
    ticksLeft = 0;
  }
}
