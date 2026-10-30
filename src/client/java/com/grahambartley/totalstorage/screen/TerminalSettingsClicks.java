package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.network.ModNetworkingClient;
import com.grahambartley.totalstorage.screenhandler.TagRowRegion;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.Objects;

public final class TerminalSettingsClicks {
  private final TerminalSettingsPanel settings;
  private final TerminalTagsPanel tags;
  private final TerminalPrompts prompts;

  public TerminalSettingsClicks(
      TerminalSettingsPanel settings, TerminalTagsPanel tags, TerminalPrompts prompts) {
    this.settings = Objects.requireNonNull(settings, "settings");
    this.tags = Objects.requireNonNull(tags, "tags");
    this.prompts = Objects.requireNonNull(prompts, "prompts");
  }

  public boolean handle(
      int syncId,
      NetworkSettingsView view,
      NetworkTagView tagView,
      boolean shiftDown,
      int windowX,
      int windowY,
      int button) {
    return switch (settings.section()) {
      case TERMINAL -> false;
      case CONTAINERS -> container(syncId, view, shiftDown, windowX, windowY, button);
      case TAGS -> tag(syncId, tagView, windowX, windowY, button);
    };
  }

  private boolean container(
      int syncId,
      NetworkSettingsView view,
      boolean shiftDown,
      int windowX,
      int windowY,
      int button) {
    final int index = settings.containerRowAt(windowX, windowY);
    final ContainerRoleEntry entry = index == TerminalSettingsLayout.NO_ROW ? null : view.at(index);
    if (entry == null) {
      return false;
    }
    final SettingsAction action =
        SettingsClick.of(
            button,
            settings.containerTargetAt(windowX, windowY),
            shiftDown,
            view.mayAdminister(),
            entry.isFiltered());
    switch (action) {
      case NONE -> {
        return false;
      }
      case REFUSED -> PromptRefusal.sound();
      case LOCATE -> ModNetworkingClient.sendContainerLocate(syncId, entry.container());
      case RENAME -> prompts.renaming().open(entry.container(), entry.name());
      case CHOOSE_TAGS -> prompts.filtering().open(entry.container());
      case CYCLE_FORWARD ->
          ModNetworkingClient.sendRoleEdit(
              syncId, entry.container(), ContainerRoleCycle.next(entry.role()));
      case CYCLE_BACK ->
          ModNetworkingClient.sendRoleEdit(
              syncId, entry.container(), ContainerRoleCycle.previous(entry.role()));
    }
    return true;
  }

  private boolean tag(int syncId, NetworkTagView view, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON) {
      return false;
    }
    final int firstVisible = settings.scroll().firstIndex();
    final NetworkTagEntry tag = tags.tagAt(view, firstVisible, windowX, windowY);
    if (tag == null) {
      tags.disarm();
      return false;
    }
    if (!view.mayAdminister()) {
      return true;
    }
    final TagRowRegion region = tags.regionAt(view, firstVisible, windowX, windowY);
    if (region == TagRowRegion.DELETE) {
      deleteOrArm(syncId, tag);
      return true;
    }
    tags.disarm();
    prompts.naming().openForRename(tag.id(), tag.displayName());
    return true;
  }

  private void deleteOrArm(int syncId, NetworkTagEntry tag) {
    if (tags.isArmed(tag.id())) {
      tags.disarm();
      ModNetworkingClient.sendTagDelete(syncId, tag.id());
      return;
    }
    tags.arm(tag.id());
  }
}
