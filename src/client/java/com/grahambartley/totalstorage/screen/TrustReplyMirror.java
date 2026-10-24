package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.TerminalTrustReplyPayload;
import java.util.Optional;

public final class TrustReplyMirror {
  private static TerminalTrustReplyPayload latest;

  private TrustReplyMirror() {}

  public static void accept(TerminalTrustReplyPayload reply) {
    latest = reply;
  }

  public static Optional<TerminalTrustReplyPayload> take(int syncId) {
    final TerminalTrustReplyPayload reply = latest;
    if (reply == null || reply.syncId() != syncId) {
      return Optional.empty();
    }
    latest = null;
    return Optional.of(reply);
  }

  public static void clear() {
    latest = null;
  }
}
