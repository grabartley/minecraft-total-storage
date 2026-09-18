package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class TagNamePrompt {
  private TagNameEntry entry;
  private String renaming = "";

  public void openForNewTag() {
    entry = new TagNameEntry("");
    renaming = "";
  }

  public void openForRename(String tagId, String currentName) {
    entry = new TagNameEntry(currentName);
    renaming = tagId == null ? "" : tagId;
  }

  public void close() {
    entry = null;
    renaming = "";
  }

  public boolean isOpen() {
    return entry != null;
  }

  public boolean isRenaming() {
    return !renaming.isEmpty();
  }

  public String renaming() {
    return renaming;
  }

  public String name() {
    return entry == null ? "" : entry.text();
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    return isOpen() ? entry.keyPressed(keyCode, selectAllHeld) : PromptKey.HANDLED;
  }

  public PromptKey charTyped(char typed) {
    return isOpen() ? entry.charTyped(typed) : PromptKey.HANDLED;
  }

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    TextPromptPanel.draw(
        context,
        textRenderer,
        TerminalScreenText.of(isRenaming() ? "tags.rename.title" : "tags.create.title"),
        TerminalScreenText.of("tags.name.hint"),
        entry.text(),
        originX,
        originY);
  }
}
