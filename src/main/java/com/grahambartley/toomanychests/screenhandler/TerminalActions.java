package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.network.TerminalActionPayload;
import com.grahambartley.toomanychests.operation.NetworkOperationService;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalActions {
  private final TerminalSettingsEdits edits;
  private final NetworkOperationService operations;

  public TerminalActions(TerminalSettingsEdits edits) {
    this(edits, new NetworkOperationService());
  }

  public TerminalActions(TerminalSettingsEdits edits, NetworkOperationService operations) {
    this.edits = Objects.requireNonNull(edits, "edits");
    this.operations = Objects.requireNonNull(operations, "operations");
  }

  public boolean act(ServerPlayerEntity player, TerminalActionPayload payload) {
    if (payload == null) {
      return false;
    }
    return switch (payload.action()) {
      case RESCAN, HIGHLIGHT -> edits.act(player, payload);
      case SORT_NETWORK -> operations.sortNetwork(player, payload.syncId()).happened();
      case CONSOLIDATE -> operations.consolidate(player, payload.syncId()).happened();
    };
  }
}
