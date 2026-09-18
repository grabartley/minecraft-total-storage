package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerNamePrompt {
  private ContainerNameEntry entry;
  private BlockPos container;

  public void open(BlockPos named, String currentName) {
    container = named == null ? null : named.toImmutable();
    entry = container == null ? null : new ContainerNameEntry(currentName);
  }

  public void close() {
    entry = null;
    container = null;
  }

  public boolean isOpen() {
    return entry != null;
  }

  public BlockPos container() {
    return container;
  }

  public String name() {
    return entry == null ? "" : entry.name();
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
        TerminalScreenText.of("settings.container.name.title"),
        TerminalScreenText.of("settings.container.name.hint"),
        entry.text(),
        originX,
        originY);
  }
}
