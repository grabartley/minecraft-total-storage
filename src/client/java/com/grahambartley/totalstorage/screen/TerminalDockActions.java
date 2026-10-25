package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.ModNetworkingClient;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.terminal.TerminalAction;
import java.util.Objects;

public final class TerminalDockActions implements TerminalActionDock.Actions {
  private final int syncId;
  private final TerminalPrompts prompts;

  public TerminalDockActions(int syncId, TerminalPrompts prompts) {
    this.syncId = syncId;
    this.prompts = Objects.requireNonNull(prompts, "prompts");
  }

  public static TerminalAction actionFor(ExpensiveOperation operation) {
    return operation == ExpensiveOperation.CONSOLIDATE
        ? TerminalAction.CONSOLIDATE
        : TerminalAction.SORT_NETWORK;
  }

  @Override
  public void depositAll() {
    ModNetworkingClient.sendDepositAllRequest(syncId);
  }

  @Override
  public void network(ExpensiveOperation operation) {
    ModNetworkingClient.sendTerminalAction(syncId, actionFor(operation));
  }

  @Override
  public void rescan() {
    ModNetworkingClient.sendTerminalAction(syncId, TerminalAction.RESCAN);
  }

  @Override
  public void outline() {
    ModNetworkingClient.sendTerminalAction(syncId, TerminalAction.HIGHLIGHT);
  }

  @Override
  public void newTag() {
    prompts.naming().openForNewTag();
  }

  @Override
  public void copyTags() {
    prompts.copying().open();
    ModNetworkingClient.sendTagSourcesRequest(syncId);
  }
}
