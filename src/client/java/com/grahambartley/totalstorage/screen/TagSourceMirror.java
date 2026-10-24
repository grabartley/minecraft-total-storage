package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.TagSourcesPayload;
import com.grahambartley.totalstorage.terminal.OwnedTerminal;
import java.util.List;

public final class TagSourceMirror {
  private static volatile int syncId = -1;
  private static volatile List<OwnedTerminal> sources = List.of();

  private TagSourceMirror() {}

  public static void accept(TagSourcesPayload payload) {
    syncId = payload.syncId();
    sources = payload.sources();
  }

  public static List<OwnedTerminal> sourcesFor(int forSyncId) {
    return syncId == forSyncId ? sources : List.of();
  }

  public static void clear() {
    syncId = -1;
    sources = List.of();
  }
}
