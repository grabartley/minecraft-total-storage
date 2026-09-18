package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.index.NetworkIndexSession;
import com.grahambartley.toomanychests.storage.ContainerAssignments;
import java.util.Objects;

public record NetworkCapacityStamp(
    NetworkIndexSession<?> session, int invalidations, ContainerAssignments containers) {
  public NetworkCapacityStamp {
    Objects.requireNonNull(session, "session");
    containers = containers == null ? ContainerAssignments.empty() : containers;
  }

  public static NetworkCapacityStamp of(NetworkIndexSession<?> session, TerminalSettings settings) {
    return new NetworkCapacityStamp(
        session,
        session.invalidationCount(),
        settings == null ? ContainerAssignments.empty() : settings.containers());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof NetworkCapacityStamp stamp
        && stamp.session == session
        && stamp.invalidations == invalidations
        && stamp.containers.equals(containers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(System.identityHashCode(session), invalidations, containers);
  }
}
