package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class TerminalPrompts {
  private final WithdrawAmountPrompt amount = new WithdrawAmountPrompt();
  private final TagNamePrompt naming = new TagNamePrompt();
  private final TagAssignmentPrompt tagging = new TagAssignmentPrompt();

  private final int syncId;

  public TerminalPrompts(int syncId) {
    this.syncId = syncId;
  }

  public TagNamePrompt naming() {
    return naming;
  }

  public boolean isAnyOpen() {
    return amount.isOpen() || naming.isOpen() || tagging.isOpen();
  }

  public boolean ownsTheKeyboard() {
    return amount.isOpen() || naming.isOpen();
  }

  public boolean isTagging() {
    return tagging.isOpen();
  }

  public void openAmount(ListingRow<ItemVariant> row) {
    amount.open(row.variant(), ceilingFor(row));
  }

  public void openTagging(ItemVariant subject) {
    tagging.open(subject);
    TerminalTagRequests.openFor(syncId, subject);
  }

  public void fit(NetworkTagView tags) {
    tagging.fit(tags);
  }

  public boolean draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView tags,
      int matching,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    if (amount.isOpen()) {
      amount.draw(context, textRenderer, originX, originY);
      return true;
    }
    if (naming.isOpen()) {
      naming.draw(context, textRenderer, originX, originY);
      return true;
    }
    if (tagging.isOpen()) {
      tagging.draw(context, textRenderer, tags, matching, originX, originY, mouseX, mouseY);
      return true;
    }
    return false;
  }

  public boolean scrolled(double verticalAmount) {
    if (ownsTheKeyboard()) {
      return true;
    }
    if (!tagging.isOpen()) {
      return false;
    }
    if (verticalAmount != 0.0) {
      tagging.scrollBy(verticalAmount > 0.0 ? -1 : 1);
    }
    return true;
  }

  public boolean mouseClicked(
      NetworkTagView tags, boolean shiftDown, int windowX, int windowY, int button) {
    if (amount.isOpen()) {
      amount.close();
      return true;
    }
    if (naming.isOpen()) {
      naming.close();
      return true;
    }
    return tagging.isOpen() && taggingClicked(tags, shiftDown, windowX, windowY, button);
  }

  public boolean keyPressed(int keyCode) {
    if (amount.isOpen()) {
      return handleAmountKey(amount.keyPressed(keyCode));
    }
    if (naming.isOpen()) {
      return handleNameKey(naming.keyPressed(keyCode));
    }
    if (!tagging.isOpen()) {
      return false;
    }
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      closeTagging();
    }
    return true;
  }

  public boolean charTyped(char typed) {
    if (amount.isOpen()) {
      return handleAmountKey(amount.charTyped(typed));
    }
    if (naming.isOpen()) {
      return handleNameKey(naming.charTyped(typed));
    }
    return tagging.isOpen();
  }

  private boolean taggingClicked(
      NetworkTagView tags, boolean shiftDown, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      closeTagging();
      return true;
    }
    final NetworkTagEntry tag = tagging.tagAt(tags, windowX, windowY);
    if (tag == null || tag.immutable()) {
      return true;
    }
    if (shiftDown) {
      ModNetworkingClient.sendTagAssignMatching(syncId, tag.id());
      return true;
    }
    ModNetworkingClient.sendTagAssign(
        syncId, tag.id(), tagging.subject(), !tags.subjectCarries(tag.id()));
    return true;
  }

  private void closeTagging() {
    tagging.close();
    TerminalTagRequests.openPanel(syncId);
  }

  private boolean handleAmountKey(PromptKey outcome) {
    if (outcome == PromptKey.CONFIRM) {
      ModNetworkingClient.sendWithdrawRequest(
          syncId, amount.variant(), amount.amount(), WithdrawDestination.INVENTORY);
    }
    if (outcome == PromptKey.CONFIRM || outcome == PromptKey.CANCEL) {
      amount.close();
    }
    return true;
  }

  private boolean handleNameKey(PromptKey outcome) {
    if (outcome == PromptKey.CONFIRM) {
      confirmName();
    }
    if (outcome == PromptKey.CANCEL) {
      naming.close();
    }
    return true;
  }

  private void confirmName() {
    if (naming.isRenaming()) {
      ModNetworkingClient.sendTagRename(syncId, naming.renaming(), naming.name());
    } else {
      ModNetworkingClient.sendTagCreate(syncId, naming.name());
    }
    naming.close();
  }

  private static int ceilingFor(ListingRow<ItemVariant> row) {
    return (int)
        Math.min(
            row.count(),
            WithdrawAmount.ceilingFor(
                row.variant().toStack().getMaxCount(), WithdrawDestination.INVENTORY));
  }
}
