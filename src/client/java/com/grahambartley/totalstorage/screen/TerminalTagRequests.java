package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.ModNetworkingClient;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class TerminalTagRequests {
  private TerminalTagRequests() {}

  public static void openPanel(int syncId) {
    ModNetworkingClient.sendTagPanelRequest(syncId, true, null);
  }

  public static void openFor(int syncId, ItemVariant subject) {
    ModNetworkingClient.sendTagPanelRequest(syncId, true, subject);
  }
}
