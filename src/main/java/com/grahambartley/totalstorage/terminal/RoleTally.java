package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.Collection;

public record RoleTally(int general, int filtered, int excluded) {
  public static final RoleTally EMPTY = new RoleTally(0, 0, 0);

  public RoleTally {
    if (general < 0 || filtered < 0 || excluded < 0) {
      throw new IllegalArgumentException("a tally cannot count below zero");
    }
  }

  public static RoleTally of(Collection<ContainerRole> roles) {
    if (roles == null || roles.isEmpty()) {
      return EMPTY;
    }
    int general = 0;
    int filtered = 0;
    int excluded = 0;
    for (final ContainerRole role : roles) {
      switch (role == null ? ContainerRole.DEFAULT : role) {
        case GENERAL -> general++;
        case FILTERED -> filtered++;
        case EXCLUDED -> excluded++;
      }
    }
    return new RoleTally(general, filtered, excluded);
  }

  public int total() {
    return general + filtered + excluded;
  }

  public boolean isEmpty() {
    return total() == 0;
  }
}
