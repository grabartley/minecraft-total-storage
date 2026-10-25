package com.grahambartley.totalstorage.screen;

import java.util.Objects;

public final class TerminalScrollbars {
  private final TerminalPrompts prompts;
  private final TerminalSettingsPanel settings;
  private final TerminalFilterColumn filters;
  private final TerminalItemsPanel items;

  public TerminalScrollbars(
      TerminalPrompts prompts,
      TerminalSettingsPanel settings,
      TerminalFilterColumn filters,
      TerminalItemsPanel items) {
    this.prompts = Objects.requireNonNull(prompts, "prompts");
    this.settings = Objects.requireNonNull(settings, "settings");
    this.filters = Objects.requireNonNull(filters, "filters");
    this.items = Objects.requireNonNull(items, "items");
  }

  public boolean dragTo(int windowY) {
    if (prompts.isDraggingScrollbar()) {
      prompts.dragScrollbarTo(windowY);
      return true;
    }
    if (settings.isDraggingScrollbar()) {
      settings.dragScrollbarTo(windowY);
      return true;
    }
    if (filters.isDraggingScrollbar()) {
      filters.dragScrollbarTo(windowY);
      return true;
    }
    if (items.scrollbar().isDragging()) {
      items.dragScrollbarTo(windowY);
      return true;
    }
    return false;
  }

  public boolean release() {
    if (prompts.isDraggingScrollbar()) {
      prompts.releaseScrollbar();
      return true;
    }
    if (settings.isDraggingScrollbar()) {
      settings.releaseScrollbar();
      return true;
    }
    if (filters.isDraggingScrollbar()) {
      filters.releaseScrollbar();
      return true;
    }
    if (items.scrollbar().isDragging()) {
      items.scrollbar().release();
      return true;
    }
    return false;
  }
}
