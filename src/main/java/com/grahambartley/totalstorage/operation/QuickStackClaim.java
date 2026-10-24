package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import java.util.Objects;

public record QuickStackClaim(ContainerAssignment assignment, NetworkTagSet tags) {
  public QuickStackClaim {
    Objects.requireNonNull(assignment, "assignment");
    tags = tags == null ? NetworkTagSet.defaults() : tags;
  }

  public QuickStackClaim strictest(QuickStackClaim other) {
    if (other == null || rank(assignment.role()) >= rank(other.assignment().role())) {
      return this;
    }
    return other;
  }

  private static int rank(ContainerRole role) {
    return switch (role) {
      case EXCLUDED -> 2;
      case FILTERED -> 1;
      case GENERAL -> 0;
    };
  }
}
