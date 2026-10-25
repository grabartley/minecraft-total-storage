package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.tag.TagNameOutcome;
import com.grahambartley.totalstorage.tag.TagNameRule;
import com.grahambartley.totalstorage.terminal.TerminalMessages;
import java.util.Set;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TagNamePrompt {
  private TagNameEntry entry;
  private String renaming = "";
  private TagNameOutcome refusal = TagNameOutcome.OK;

  public void openForNewTag() {
    open(new TagNameEntry(""), "");
  }

  public void openForRename(String tagId, String currentName) {
    open(new TagNameEntry(currentName), tagId == null ? "" : tagId);
  }

  private void open(TagNameEntry opened, String renamed) {
    entry = opened;
    renaming = renamed;
    refusal = TagNameOutcome.OK;
  }

  public void close() {
    entry = null;
    renaming = "";
    refusal = TagNameOutcome.OK;
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

  public TagNameOutcome refusal() {
    return refusal;
  }

  public boolean confirm(Set<String> knownTagIds) {
    refusal = TagNameRule.check(name(), knownTagIds, renaming);
    return refusal.isAccepted();
  }

  public PromptKey keyPressed(int keyCode, boolean selectAllHeld) {
    if (!isOpen()) {
      return PromptKey.HANDLED;
    }
    final String before = entry.text();
    return forgetRefusalOnEdit(before, entry.keyPressed(keyCode, selectAllHeld));
  }

  public PromptKey charTyped(char typed) {
    if (!isOpen()) {
      return PromptKey.HANDLED;
    }
    final String before = entry.text();
    return forgetRefusalOnEdit(before, entry.charTyped(typed));
  }

  private PromptKey forgetRefusalOnEdit(String before, PromptKey outcome) {
    if (!before.equals(entry.text())) {
      refusal = TagNameOutcome.OK;
    }
    return outcome;
  }

  public void draw(DrawContext context, TextRenderer textRenderer, int originX, int originY) {
    if (!isOpen()) {
      return;
    }
    TextPromptPanel.draw(
        context,
        textRenderer,
        TerminalScreenText.of(isRenaming() ? "tags.rename.title" : "tags.create.title"),
        entry.text(),
        TerminalScreenText.of("tags.name.placeholder"),
        refusal.isAccepted() ? null : TagNameRefusalText.of(refusal),
        TextPromptPanel.saveKeys(),
        originX,
        originY);
  }

  public Text fullRefusal() {
    return refusal.isAccepted() ? null : TerminalMessages.tagNameRejected(refusal);
  }
}
