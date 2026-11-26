package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;

public enum SortOutcome {
  NOT_OFFERED,
  NO_OPEN_SCREEN,
  SCREEN_NOT_SORTABLE,
  NOT_OPENABLE,
  RATE_LIMITED,
  NOTHING_TO_SORT,
  ALREADY_SORTED,
  FAILED,
  SORTED;

  public TerminalNoticeTone tone() {
    return switch (this) {
      case SORTED, ALREADY_SORTED, NOTHING_TO_SORT -> TerminalNoticeTone.DONE;
      case RATE_LIMITED -> TerminalNoticeTone.PARTIAL;
      case NOT_OFFERED, NO_OPEN_SCREEN, SCREEN_NOT_SORTABLE, NOT_OPENABLE, FAILED ->
          TerminalNoticeTone.FAILED;
    };
  }

  public boolean wasSorted() {
    return this == SORTED;
  }

  public boolean wasRefused() {
    return this == NOT_OFFERED
        || this == NO_OPEN_SCREEN
        || this == SCREEN_NOT_SORTABLE
        || this == NOT_OPENABLE
        || this == RATE_LIMITED;
  }
}
