package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.network.ModNetworkingClient;
import java.util.Objects;

public final class TerminalSettingsEditSender implements TerminalSettingsButtons.Edits {
  private final int syncId;
  private final TerminalPrompts prompts;

  public TerminalSettingsEditSender(int syncId, TerminalPrompts prompts) {
    this.syncId = syncId;
    this.prompts = Objects.requireNonNull(prompts, "prompts");
  }

  @Override
  public void access(TerminalAccessMode mode) {
    ModNetworkingClient.sendAccessEdit(syncId, mode);
  }

  @Override
  public void trusted() {
    prompts.trusting().open();
  }

  @Override
  public void radius(int blocks) {
    ModNetworkingClient.sendRadiusEdit(syncId, blocks);
  }
}
