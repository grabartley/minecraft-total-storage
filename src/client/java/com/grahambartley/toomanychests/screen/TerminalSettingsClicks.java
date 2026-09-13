package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.screenhandler.TagRowRegion;
import com.grahambartley.toomanychests.screenhandler.TagsSectionLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.Objects;

public final class TerminalSettingsClicks {
  private final TerminalSettingsPanel settings;
  private final TerminalTagsPanel tags;
  private final TagNamePrompt naming;
  private final TagSourcePrompt copying;

  public TerminalSettingsClicks(
      TerminalSettingsPanel settings,
      TerminalTagsPanel tags,
      TagNamePrompt naming,
      TagSourcePrompt copying) {
    this.settings = Objects.requireNonNull(settings, "settings");
    this.tags = Objects.requireNonNull(tags, "tags");
    this.naming = Objects.requireNonNull(naming, "naming");
    this.copying = Objects.requireNonNull(copying, "copying");
  }

  public boolean handle(
      int syncId,
      NetworkSettingsView view,
      NetworkTagView tagView,
      int firstVisibleTag,
      int windowX,
      int windowY,
      int button) {
    final boolean mayAdminister = view.mayAdminister();
    return switch (settings.section()) {
      case TERMINAL -> terminal(syncId, view, windowX, windowY, button, mayAdminister);
      case CONTAINERS -> container(syncId, view, windowX, windowY, button, mayAdminister);
      case TAGS -> tag(syncId, tagView, firstVisibleTag, windowX, windowY, button);
    };
  }

  private boolean terminal(
      int syncId,
      NetworkSettingsView view,
      int windowX,
      int windowY,
      int button,
      boolean mayAdminister) {
    final SettingsControl control = SettingsControl.at(windowX, windowY);
    if (control.isSomething()) {
      if (mayAdminister && button == GridClick.LEFT_BUTTON) {
        ModNetworkingClient.sendRadiusEdit(
            syncId, view.radius() + (control == SettingsControl.RADIUS_UP ? 1 : -1));
      }
      return true;
    }
    final TerminalSetting setting = settings.settingAt(windowX, windowY);
    final SettingsAction action =
        SettingsClick.of(button, setting != null && setting.cyclesOnRowClick(), mayAdminister);
    if (action == SettingsAction.NONE) {
      return setting != null;
    }
    if (action != SettingsAction.REFUSED) {
      ModNetworkingClient.sendAccessEdit(
          syncId,
          action == SettingsAction.CYCLE_BACK
              ? AccessModeCycle.previous(view.accessMode())
              : AccessModeCycle.next(view.accessMode()));
    }
    return true;
  }

  private boolean container(
      int syncId,
      NetworkSettingsView view,
      int windowX,
      int windowY,
      int button,
      boolean mayAdminister) {
    final int index = settings.containerRowAt(windowX, windowY);
    final ContainerRoleEntry entry = index == TerminalSettingsLayout.NO_ROW ? null : view.at(index);
    final SettingsAction action = SettingsClick.of(button, entry != null, mayAdminister);
    if (action == SettingsAction.NONE) {
      return false;
    }
    if (action != SettingsAction.REFUSED) {
      ModNetworkingClient.sendRoleEdit(
          syncId,
          entry.container(),
          action == SettingsAction.CYCLE_BACK
              ? ContainerRoleCycle.previous(entry.role())
              : ContainerRoleCycle.next(entry.role()));
    }
    return true;
  }

  private boolean tag(
      int syncId, NetworkTagView view, int firstVisibleTag, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON) {
      return false;
    }
    if (TagsSectionLayout.isOverCopy(windowX, windowY)) {
      if (view.mayAdminister()) {
        copying.open();
        ModNetworkingClient.sendTagSourcesRequest(syncId);
      }
      return true;
    }
    if (TagsSectionLayout.isOverNewTag(windowX, windowY)) {
      if (view.mayAdminister()) {
        naming.openForNewTag();
      }
      return true;
    }
    final TagRowRegion region = TagsSectionLayout.regionAt(windowX, windowY);
    final NetworkTagEntry tag = tags.tagAt(view, firstVisibleTag, windowX, windowY);
    if (!region.isSomething() || tag == null) {
      return false;
    }
    if (!view.mayAdminister()) {
      return true;
    }
    if (region == TagRowRegion.DELETE) {
      deleteOrArm(syncId, tag);
      return true;
    }
    tags.disarm();
    naming.openForRename(tag.id(), tag.displayName());
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
