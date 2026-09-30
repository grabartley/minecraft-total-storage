package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.tag.TagSetImportMode;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
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
  private final ContainerNamePrompt renaming = new ContainerNamePrompt();
  private final ContainerFilterPrompt filtering = new ContainerFilterPrompt();
  private final TrustPrompt trusting = new TrustPrompt();

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

  public ContainerNamePrompt renaming() {
    return renaming;
  }

  public ContainerFilterPrompt filtering() {
    return filtering;
  }

  public TrustPrompt trusting() {
    return trusting;
  }

  public boolean isAnyOpen() {
    return amount.isOpen()
        || naming.isOpen()
        || renaming.isOpen()
        || tagging.isOpen()
        || filtering.isOpen()
        || trusting.isOpen()
        || copying.isOpen();
  }

  public boolean ownsTheKeyboard() {
    return amount.isOpen() || naming.isOpen() || renaming.isOpen() || trusting.isOpen();
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

  public void fit(NetworkSettingsView settings, NetworkTagView tags) {
    tagging.fit(tags);
    filtering.fit(tags);
    if (filtering.isOpen() && settings.answered() && !stillFiltered(settings)) {
      filtering.close();
    }
    copying.fit(sources());
    trusting.fit(settings);
    if (trusting.isOpen() && settings.answered() && !settings.mayAdminister()) {
      trusting.close();
    }
  }

  public void tick() {
    TrustReplyMirror.take(syncId).ifPresent(trusting::answered);
  }

  private boolean stillFiltered(NetworkSettingsView settings) {
    final ContainerRoleEntry row = filtering.rowIn(settings);
    return row != null && row.isFiltered();
  }

  private List<OwnedTerminal> sources() {
    return TagSourceMirror.sourcesFor(syncId);
  }

  public boolean draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkSettingsView settings,
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
    if (renaming.isOpen()) {
      renaming.draw(context, textRenderer, originX, originY);
      return true;
    }
    if (filtering.isOpen()) {
      filtering.draw(context, textRenderer, settings, tags, originX, originY, mouseX, mouseY);
      return true;
    }
    if (trusting.isOpen()) {
      trusting.draw(context, textRenderer, settings, originX, originY, mouseX, mouseY);
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

  private TagPromptList openList() {
    if (amount.isOpen() || naming.isOpen() || renaming.isOpen()) {
      return null;
    }
    if (filtering.isOpen()) {
      return filtering.list();
    }
    if (trusting.isOpen()) {
      return trusting.list();
    }
    if (tagging.isOpen()) {
      return tagging.list();
    }
    return copying.isOpen() ? copying.list() : null;
  }

  public boolean isDraggingScrollbar() {
    final TagPromptList list = openList();
    return list != null && list.isDraggingScrollbar();
  }

  public void dragScrollbarTo(int windowY) {
    final TagPromptList list = openList();
    if (list != null) {
      list.dragScrollbarTo(windowY);
    }
  }

  public void releaseScrollbar() {
    final TagPromptList list = openList();
    if (list != null) {
      list.releaseScrollbar();
    }
  }

  public boolean scrolled(double verticalAmount) {
    final TagPromptList list = openList();
    if (list == null) {
      return ownsTheKeyboard();
    }
    if (verticalAmount != 0.0) {
      list.scrollBy(verticalAmount > 0.0 ? -1 : 1);
    }
    return true;
  }

  public boolean mouseClicked(
      NetworkSettingsView settings,
      NetworkTagView tags,
      boolean shiftDown,
      int windowX,
      int windowY,
      int button) {
    if (amount.isOpen()) {
      amount.close();
      return true;
    }
    if (naming.isOpen()) {
      naming.close();
      return true;
    }
    if (renaming.isOpen()) {
      renaming.close();
      return true;
    }
    final TagPromptList list = openList();
    if (list != null
        && button == GridClick.LEFT_BUTTON
        && TagPromptLayout.isOverScrollbar(windowX, windowY)) {
      list.grabScrollbar(windowX, windowY);
      return true;
    }
    if (filtering.isOpen()) {
      return filteringClicked(settings, tags, windowX, windowY, button);
    }
    if (trusting.isOpen()) {
      return trustingClicked(settings, windowX, windowY, button);
    }
    if (copying.isOpen()) {
      return copyingClicked(shiftDown, windowX, windowY, button);
    }
    return tagging.isOpen() && taggingClicked(tags, shiftDown, windowX, windowY, button);
  }

  private boolean filteringClicked(
      NetworkSettingsView settings, NetworkTagView tags, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      filtering.close();
      return true;
    }
    final ContainerRoleEntry row = filtering.rowIn(settings);
    final NetworkTagEntry tag = filtering.tagAt(tags, windowX, windowY);
    if (row == null || tag == null) {
      return true;
    }
    if (!settings.mayAdminister()) {
      PromptRefusal.sound();
      return true;
    }
    ModNetworkingClient.sendContainerFilterTag(
        syncId, row.container(), tag.id(), !row.accepts(tag.id()));
    return true;
  }

  private boolean trustingClicked(
      NetworkSettingsView settings, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      trusting.close();
      return true;
    }
    final String removed = trusting.removalAt(settings, windowX, windowY);
    if (removed != null) {
      ModNetworkingClient.sendTrustEdit(syncId, removed, false);
    }
    return true;
  }

  private boolean copyingClicked(boolean shiftDown, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      copying.close();
      return true;
    }
    final OwnedTerminal source = copying.sourceAt(sources(), windowX, windowY);
    if (source == null) {
      copying.disarm();
      return true;
    }
    if (!shiftDown) {
      copying.disarm();
      send(source, TagSetImportMode.MERGE);
      return true;
    }
    if (!source.holdsAnythingToCopy()) {
      copying.disarm();
      return true;
    }
    if (copying.isArmed(source)) {
      send(source, TagSetImportMode.REPLACE);
      return true;
    }
    copying.armForReplace(source);
    return true;
  }

  private void send(OwnedTerminal source, TagSetImportMode mode) {
    ModNetworkingClient.sendTagSetCopy(syncId, source.pos(), mode);
    copying.close();
  }

  public boolean keyPressed(int keyCode, int modifiers, NetworkTagView tags) {
    final boolean selectAllHeld = PromptModifiers.isSelectAllHeld(modifiers);
    if (amount.isOpen()) {
      return handleAmountKey(amount.keyPressed(keyCode, selectAllHeld));
    }
    if (naming.isOpen()) {
      return handleNameKey(naming.keyPressed(keyCode, selectAllHeld), tags);
    }
    if (renaming.isOpen()) {
      return handleContainerNameKey(renaming.keyPressed(keyCode, selectAllHeld));
    }
    if (trusting.isOpen()) {
      return handleTrustKey(trusting.keyPressed(keyCode, selectAllHeld));
    }
    if (filtering.isOpen()) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        filtering.close();
      }
      return true;
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
      return soundIfRefused(naming.charTyped(typed));
    }
    if (renaming.isOpen()) {
      return handleContainerNameKey(renaming.charTyped(typed));
    }
    if (trusting.isOpen()) {
      return soundIfRefused(trusting.charTyped(typed));
    }
    return tagging.isOpen() || filtering.isOpen();
  }

  private boolean taggingClicked(
      NetworkTagView tags, boolean shiftDown, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !TagPromptLayout.isOverPrompt(windowX, windowY)) {
      closeTagging();
      return true;
    }
    final NetworkTagEntry tag = tagging.tagAt(tags, windowX, windowY);
    if (tag == null) {
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
    if (outcome == PromptKey.REFUSED) {
      PromptRefusal.sound();
      return true;
    }
    if (outcome == PromptKey.CONFIRM) {
      reportClampIfAny();
      ModNetworkingClient.sendWithdrawRequest(
          syncId, amount.variant(), amount.amount(), WithdrawDestination.INVENTORY);
    }
    if (outcome == PromptKey.CONFIRM || outcome == PromptKey.CANCEL) {
      amount.close();
    }
    return true;
  }

  private void reportClampIfAny() {
    if (!amount.asksForMoreThanThereIs()) {
      return;
    }
    TerminalNoticeMirror.accept(
        syncId,
        TerminalScreenText.of("amount.clamped", amount.requested(), amount.amount()),
        false);
  }

  private static boolean soundIfRefused(PromptKey outcome) {
    if (outcome == PromptKey.REFUSED) {
      PromptRefusal.sound();
    }
    return true;
  }

  private boolean handleNameKey(PromptKey outcome, NetworkTagView tags) {
    if (outcome == PromptKey.REFUSED) {
      return soundIfRefused(outcome);
    }
    if (outcome == PromptKey.CONFIRM) {
      if (!naming.confirm(tags.ids())) {
        PromptRefusal.sound();
        return true;
      }
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

  private boolean handleContainerNameKey(PromptKey outcome) {
    if (outcome == PromptKey.REFUSED) {
      PromptRefusal.sound();
      return true;
    }
    if (outcome == PromptKey.CONFIRM) {
      ModNetworkingClient.sendContainerName(syncId, renaming.container(), renaming.name());
      renaming.close();
    }
    if (outcome == PromptKey.CANCEL) {
      renaming.close();
    }
    return true;
  }

  private boolean handleTrustKey(PromptKey outcome) {
    if (outcome == PromptKey.REFUSED) {
      return soundIfRefused(outcome);
    }
    if (outcome == PromptKey.CONFIRM) {
      ModNetworkingClient.sendTrustEdit(syncId, trusting.name(), true);
    }
    if (outcome == PromptKey.CANCEL) {
      trusting.close();
    }
    return true;
  }

  private static int ceilingFor(ListingRow<ItemVariant> row) {
    return (int)
        Math.min(
            row.count(),
            WithdrawAmount.ceilingFor(
                row.variant().toStack().getMaxCount(), WithdrawDestination.INVENTORY));
  }
}
