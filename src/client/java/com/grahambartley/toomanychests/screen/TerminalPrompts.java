package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.tag.TagSetImportMode;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import com.grahambartley.toomanychests.terminal.OwnedTerminal;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class TerminalPrompts {
  private final WithdrawAmountPrompt amount = new WithdrawAmountPrompt();
  private final TagNamePrompt naming = new TagNamePrompt();
  private final TagAssignmentPrompt tagging = new TagAssignmentPrompt();
  private final TagSourcePrompt copying = new TagSourcePrompt();

  private final int syncId;

  public TerminalPrompts(int syncId) {
    this.syncId = syncId;
  }

  public TagNamePrompt naming() {
    return naming;
  }

  public TagSourcePrompt copying() {
    return copying;
  }

  public boolean isAnyOpen() {
    return amount.isOpen() || naming.isOpen() || tagging.isOpen() || copying.isOpen();
  }

  public boolean ownsTheKeyboard() {
    return amount.isOpen() || naming.isOpen();
  }

  public boolean isTagging() {
    return tagging.isOpen() || copying.isOpen();
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
    copying.fit(sources());
  }

  private List<OwnedTerminal> sources() {
    return TagSourceMirror.sourcesFor(syncId);
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
    if (copying.isOpen()) {
      copying.draw(context, textRenderer, sources(), originX, originY, mouseX, mouseY);
      return true;
    }
    return false;
  }

  public boolean scrolled(double verticalAmount) {
    if (ownsTheKeyboard()) {
      return true;
    }
    if (copying.isOpen()) {
      if (verticalAmount != 0.0) {
        copying.scrollBy(verticalAmount > 0.0 ? -1 : 1);
      }
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
    if (copying.isOpen()) {
      return copyingClicked(shiftDown, windowX, windowY, button);
    }
    return tagging.isOpen() && taggingClicked(tags, shiftDown, windowX, windowY, button);
  }

  private boolean copyingClicked(boolean shiftDown, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      copying.close();
      return true;
    }
    final OwnedTerminal source = copying.sourceAt(sources(), windowX, windowY);
    if (source == null) {
      return true;
    }
    ModNetworkingClient.sendTagSetCopy(
        syncId, source.pos(), shiftDown ? TagSetImportMode.REPLACE : TagSetImportMode.MERGE);
    copying.close();
    return true;
  }

  public boolean keyPressed(int keyCode) {
    if (amount.isOpen()) {
      return handleAmountKey(amount.keyPressed(keyCode));
    }
    if (naming.isOpen()) {
      return handleNameKey(naming.keyPressed(keyCode));
    }
    if (copying.isOpen()) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        copying.close();
      }
      return true;
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
