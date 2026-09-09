package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingStatus;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class TerminalScreen extends HandledScreen<TerminalScreenHandler> {
  private static final Identifier PANEL = TerminalSprites.of("panel/background");
  private static final Identifier INSET = TerminalSprites.of("panel/inset");
  private static final Identifier TAB_SELECTED = TerminalSprites.of("tab/selected");
  private static final Identifier TAB_UNSELECTED = TerminalSprites.of("tab/unselected");
  private static final Identifier ITEMS_ICON = TerminalSprites.of("icon/items");
  private static final Identifier SETTINGS_ICON = TerminalSprites.of("icon/settings");
  private static final Identifier DEPOSIT_ALL_ICON = TerminalSprites.of("icon/deposit_all");
  private static final Identifier RESCAN_ICON = TerminalSprites.of("icon/rescan");
  private static final Identifier HIGHLIGHT_ICON = TerminalSprites.of("icon/highlight");

  private final TerminalItemsPanel items = new TerminalItemsPanel();
  private final TerminalSettingsPanel settings = new TerminalSettingsPanel();
  private final WithdrawAmountPrompt prompt = new WithdrawAmountPrompt();

  private final ListingCriteria requested = new ListingCriteria();

  private TerminalTab tab = TerminalTab.ITEMS;
  private TerminalSearchBox search;
  private TerminalIconButton depositAll;
  private TerminalIconButton rescan;
  private TerminalIconButton highlight;

  private NetworkSettingsView settingsThisFrame = NetworkSettingsView.pending();

  private ListingWindow requestedContainers = ListingWindow.EMPTY;

  private TerminalTab hoveredTab;
  private int hoveredCell = TerminalScreenLayout.NO_CELL;
  private int hoveredWindowX = Integer.MIN_VALUE;
  private int hoveredWindowY = Integer.MIN_VALUE;

  public TerminalScreen(TerminalScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
    this.backgroundWidth = TerminalScreenLayout.WINDOW_WIDTH;
    this.backgroundHeight = TerminalScreenLayout.WINDOW_HEIGHT;
    this.titleX = TerminalScreenLayout.TITLE_X;
    this.titleY = TerminalScreenLayout.TITLE_Y;
    this.playerInventoryTitleX = TerminalScreenLayout.INVENTORY_LABEL_X;
    this.playerInventoryTitleY = TerminalScreenLayout.INVENTORY_LABEL_Y;
  }

  @Override
  protected void init() {
    super.init();
    search = new TerminalSearchBox(textRenderer, x, y, search == null ? "" : search.text());
    addDrawableChild(search.widget());
    depositAll = action(0, DEPOSIT_ALL_ICON, "deposit_all", this::depositEverything);
    rescan = action(0, RESCAN_ICON, "rescan", () -> act(TerminalAction.RESCAN));
    highlight = action(1, HIGHLIGHT_ICON, "highlight", () -> act(TerminalAction.HIGHLIGHT));
    setInitialFocus(search.widget());
    updateWidgets(TerminalListingMirror.page().status());
    if (TerminalListingMirror.isShowing(handler.syncId)) {
      return;
    }
    TerminalListingMirror.opened(handler.syncId);
    NetworkSettingsMirror.opened(handler.syncId);
    requestVisibleRows();
  }

  private TerminalIconButton action(int row, Identifier icon, String label, Runnable onPress) {
    final TerminalIconButton button =
        new TerminalIconButton(
            x + TerminalScreenLayout.TOOLBAR_X,
            y + TerminalScreenLayout.actionY(row),
            icon,
            TerminalScreenText.of(label),
            onPress);
    addDrawableChild(button);
    return button;
  }

  @Override
  public void removed() {
    super.removed();
    TerminalListingMirror.closed();
    NetworkSettingsMirror.closed();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    items.beginFrame(TerminalListingMirror.page());
    settingsThisFrame = NetworkSettingsMirror.view();
    settings.fit(settingsThisFrame);
    if (requested.isNewQuery(search.text())) {
      items.scroll().scrollTo(0);
    }
    if (!tab.isSettings() && requested.needsRequest(items.scroll().window(), search.text())) {
      requestVisibleRows();
    }
    if (tab.isSettings() && !settings.scroll().window().equals(requestedContainers)) {
      requestVisibleContainers();
    }
    updateWidgets(items.page().status());
    updateHovered(mouseX, mouseY);
    super.render(context, mouseX, mouseY, delta);
    if (prompt.isOpen()) {
      prompt.draw(context, textRenderer, x, y);
      return;
    }
    drawMouseoverTooltip(context, mouseX, mouseY);
    drawCellTooltip(context, mouseX, mouseY);
    drawContainerTooltip(context, mouseX, mouseY);
    drawTabTooltip(context, mouseX, mouseY);
  }

  private void drawTabTooltip(DrawContext context, int mouseX, int mouseY) {
    if (hoveredTab != null) {
      context.drawTooltip(textRenderer, TerminalScreenText.of(hoveredTab.label()), mouseX, mouseY);
    }
  }

  private void updateHovered(int mouseX, int mouseY) {
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    if (prompt.isOpen()) {
      hoveredTab = null;
      hoveredCell = TerminalScreenLayout.NO_CELL;
      hoveredWindowX = Integer.MIN_VALUE;
      hoveredWindowY = Integer.MIN_VALUE;
      return;
    }
    hoveredTab = TerminalTab.at(windowX, windowY);
    hoveredCell =
        tab.isSettings() ? TerminalScreenLayout.NO_CELL : items.hoveredCell(windowX, windowY);
    hoveredWindowX = windowX;
    hoveredWindowY = windowY;
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    context.drawGuiTexture(PANEL, x, y, backgroundWidth, backgroundHeight);
    if (!tab.isSettings()) {
      search.drawBox(context, x, y);
    }
    context.drawGuiTexture(
        INSET,
        x + TerminalScreenLayout.INSET_X,
        y + TerminalScreenLayout.INSET_Y,
        TerminalScreenLayout.INSET_WIDTH,
        TerminalScreenLayout.INSET_HEIGHT);
    if (!tab.isSettings()) {
      items.drawCells(context, x, y, hoveredCell);
      items.drawScrollbar(context, x, y);
    }
    drawTabs(context);
    for (final Slot slot : handler.slots) {
      context.drawGuiTexture(
          INSET,
          x + slot.x - 1,
          y + slot.y - 1,
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  private void drawTabs(DrawContext context) {
    drawTab(context, TerminalTab.ITEMS, ITEMS_ICON);
    drawTab(context, TerminalTab.SETTINGS, SETTINGS_ICON);
  }

  private void drawTab(DrawContext context, TerminalTab drawn, Identifier icon) {
    final int left = x + TerminalScreenLayout.TOOLBAR_X;
    final int top = y + TerminalScreenLayout.tabY(drawn.index());
    context.drawGuiTexture(
        drawn == tab ? TAB_SELECTED : TAB_UNSELECTED,
        left,
        top,
        TerminalScreenLayout.BUTTON_SIZE,
        TerminalScreenLayout.BUTTON_SIZE);
    context.drawGuiTexture(
        icon,
        left + TerminalScreenLayout.ICON_INSET,
        top + TerminalScreenLayout.ICON_INSET,
        TerminalScreenLayout.ICON_SIZE,
        TerminalScreenLayout.ICON_SIZE);
  }

  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    super.drawForeground(context, mouseX, mouseY);
    if (tab.isSettings()) {
      settings.draw(context, textRenderer, settingsThisFrame, 0, 0, hoveredWindowX, hoveredWindowY);
      return;
    }
    items.drawRows(context, textRenderer);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (prompt.isOpen()) {
      return true;
    }
    if (verticalAmount == 0.0) {
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    final int notches = verticalAmount > 0.0 ? -1 : 1;
    if (tab.isSettings()) {
      if (settings.scrolls()
          && settings.scroll().overflows()
          && (TerminalSettingsLayout.rowAt(windowX(mouseX), windowY(mouseY))
                  != TerminalSettingsLayout.NO_ROW
              || settings.isOverScrollbar(windowX(mouseX), windowY(mouseY)))) {
        settings.scroll().scrollBy(notches);
        return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    if (items.scroll().overflows()
        && TerminalScreenLayout.isOverListing(windowX(mouseX), windowY(mouseY))) {
      items.scroll().scrollBy(notches);
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (prompt.isOpen()) {
      prompt.close();
      return true;
    }
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    final TerminalTab clicked = TerminalTab.at(windowX, windowY);
    if (clicked != null) {
      showTab(clicked);
      return true;
    }
    if (tab.isSettings()) {
      if (button == GridClick.LEFT_BUTTON
          && settings.isOverScrollbar(windowX, windowY)
          && settings.grabScrollbar(windowY)) {
        return true;
      }
      return handleSettingsClick(windowX, windowY, button)
          || super.mouseClicked(mouseX, mouseY, button);
    }
    if (button == GridClick.LEFT_BUTTON && TerminalScreenLayout.isOverSearch(windowX, windowY)) {
      setFocused(search.widget());
      search.widget().mouseClicked(mouseX, mouseY, button);
      return true;
    }
    if (button == GridClick.LEFT_BUTTON
        && TerminalScreenLayout.isOverScrollbar(windowX, windowY)
        && items.scrollbar().grab(windowY - TerminalScreenLayout.SCROLLBAR_Y)) {
      return true;
    }
    if (handleGridClick(windowX, windowY, button)) {
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void showTab(TerminalTab shown) {
    if (tab == shown) {
      return;
    }
    tab = shown;
    if (tab.isSettings()) {
      requestVisibleContainers();
      return;
    }
    stopAskingForContainers();
    search.setVisible(true);
    setFocused(search.widget());
    requestVisibleRows();
  }

  private boolean handleGridClick(int windowX, int windowY, int button) {
    final int cell = TerminalScreenLayout.cellAt(windowX, windowY);
    if (cell == TerminalScreenLayout.NO_CELL) {
      return false;
    }
    final ListingRow<ItemVariant> row = items.page().status().hasRows() ? items.rowAt(cell) : null;
    final GridAction action =
        GridClick.of(button, hasShiftDown(), !handler.getCursorStack().isEmpty(), row != null);
    if (action == GridAction.NONE) {
      return false;
    }
    if (action == GridAction.PROMPT_FOR_AMOUNT) {
      prompt.open(row.variant(), promptCeiling(row));
      return true;
    }
    TerminalGridRequests.send(action, handler.syncId, row == null ? null : row.variant());
    return true;
  }

  private boolean handleSettingsClick(int windowX, int windowY, int button) {
    final SettingsSection clicked = SettingsSection.at(windowX, windowY);
    if (clicked != null) {
      showSection(clicked);
      return true;
    }
    final boolean mayAdminister = settingsThisFrame.mayAdminister();
    if (settings.section() == SettingsSection.TERMINAL) {
      final SettingsControl control = SettingsControl.at(windowX, windowY);
      if (control.isSomething()) {
        if (mayAdminister && button == GridClick.LEFT_BUTTON) {
          stepRadius(control);
        }
        return true;
      }
      return handleSettingClick(windowX, windowY, button, mayAdminister);
    }
    return handleContainerClick(windowX, windowY, button, mayAdminister);
  }

  private boolean handleSettingClick(int windowX, int windowY, int button, boolean mayAdminister) {
    final TerminalSetting setting = settings.settingAt(windowX, windowY);
    final SettingsAction action =
        SettingsClick.of(button, setting != null && setting.cyclesOnRowClick(), mayAdminister);
    if (action == SettingsAction.NONE) {
      return setting != null;
    }
    if (action != SettingsAction.REFUSED) {
      ModNetworkingClient.sendAccessEdit(
          handler.syncId,
          action == SettingsAction.CYCLE_BACK
              ? AccessModeCycle.previous(settingsThisFrame.accessMode())
              : AccessModeCycle.next(settingsThisFrame.accessMode()));
    }
    return true;
  }

  private boolean handleContainerClick(
      int windowX, int windowY, int button, boolean mayAdminister) {
    final int index = settings.containerRowAt(windowX, windowY);
    final ContainerRoleEntry entry =
        index == TerminalSettingsLayout.NO_ROW ? null : settingsThisFrame.at(index);
    final SettingsAction action = SettingsClick.of(button, entry != null, mayAdminister);
    if (action == SettingsAction.NONE) {
      return false;
    }
    if (action != SettingsAction.REFUSED) {
      ModNetworkingClient.sendRoleEdit(
          handler.syncId,
          entry.container(),
          action == SettingsAction.CYCLE_BACK
              ? ContainerRoleCycle.previous(entry.role())
              : ContainerRoleCycle.next(entry.role()));
    }
    return true;
  }

  private void showSection(SettingsSection shown) {
    if (settings.section() != shown) {
      settings.show(shown);
      requestVisibleContainers();
    }
  }

  private void stepRadius(SettingsControl control) {
    ModNetworkingClient.sendRadiusEdit(
        handler.syncId,
        settingsThisFrame.radius() + (control == SettingsControl.RADIUS_UP ? 1 : -1));
  }

  @Override
  protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
    if (actionType == SlotActionType.QUICK_MOVE
        && slot != null
        && slot.inventory instanceof PlayerInventory
        && slot.hasStack()) {
      ModNetworkingClient.sendDepositRequest(
          DepositRequestPayload.wholeSlot(handler.syncId, slot.getIndex()));
      return;
    }
    super.onMouseClick(slot, slotId, button, actionType);
  }

  private void updateWidgets(ListingStatus status) {
    final boolean settingsShowing = tab.isSettings();
    depositAll.active = status != ListingStatus.OFFLINE;
    depositAll.visible = !prompt.isOpen() && !settingsShowing;
    rescan.visible = !prompt.isOpen() && settingsShowing;
    highlight.visible = rescan.visible;
    rescan.active = settingsThisFrame.powered() && settingsThisFrame.mayAdminister();
    highlight.active = settingsThisFrame.powered();
    search.setVisible(!settingsShowing);
  }

  private boolean isDepositAllKey(int keyCode, int scanCode) {
    return TerminalKeyBindings.isDepositAll(keyCode, scanCode)
        && !tab.isSettings()
        && !search.isFocused();
  }

  private void depositEverything() {
    ModNetworkingClient.sendDepositAllRequest(handler.syncId);
  }

  private void act(TerminalAction action) {
    ModNetworkingClient.sendTerminalAction(handler.syncId, action);
  }

  private static int promptCeiling(ListingRow<ItemVariant> row) {
    return (int)
        Math.min(
            row.count(),
            WithdrawAmount.ceilingFor(
                row.variant().toStack().getMaxCount(), WithdrawDestination.INVENTORY));
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (prompt.isOpen()) {
      return true;
    }
    if (settings.isDraggingScrollbar()) {
      settings.dragScrollbarTo(windowY(mouseY));
      return true;
    }
    if (items.scrollbar().isDragging()) {
      items.scrollbar().dragTo(windowY(mouseY) - TerminalScreenLayout.SCROLLBAR_Y);
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (prompt.isOpen()) {
      return handlePromptKey(prompt.keyPressed(keyCode));
    }
    if (isDepositAllKey(keyCode, scanCode)) {
      depositEverything();
      return true;
    }
    if (tab.isSettings()) {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
    return switch (SearchKeyOutcome.of(
        keyCode == GLFW.GLFW_KEY_ESCAPE, search.isFocused(), search.isSearching())) {
      case CLEAR_SEARCH -> {
        search.clear();
        yield true;
      }
      case TYPE -> {
        search.keyPressed(keyCode, scanCode, modifiers);
        yield true;
      }
      case CLOSE_SCREEN, PASS_THROUGH -> super.keyPressed(keyCode, scanCode, modifiers);
    };
  }

  @Override
  public boolean charTyped(char typed, int modifiers) {
    if (prompt.isOpen()) {
      return handlePromptKey(prompt.charTyped(typed));
    }
    return !tab.isSettings() && super.charTyped(typed, modifiers);
  }

  private boolean handlePromptKey(PromptKey outcome) {
    if (outcome == PromptKey.CONFIRM) {
      ModNetworkingClient.sendWithdrawRequest(
          handler.syncId, prompt.variant(), prompt.amount(), WithdrawDestination.INVENTORY);
    }
    if (outcome == PromptKey.CONFIRM || outcome == PromptKey.CANCEL) {
      prompt.close();
    }
    return true;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GridClick.LEFT_BUTTON && settings.isDraggingScrollbar()) {
      settings.releaseScrollbar();
      return true;
    }
    if (button == GridClick.LEFT_BUTTON && items.scrollbar().isDragging()) {
      items.scrollbar().release();
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private void requestVisibleRows() {
    requested.requested(items.scroll().window(), search.text());
    ModNetworkingClient.sendListingRequest(
        handler.syncId,
        requested.window(),
        requested.search(),
        TagCombinationMode.DEFAULT,
        Set.of());
  }

  private void requestVisibleContainers() {
    requestedContainers = settings.scroll().window();
    ModNetworkingClient.sendSettingsRequest(
        handler.syncId, requestedContainers.offset(), requestedContainers.length());
  }

  private void stopAskingForContainers() {
    requestedContainers = ListingWindow.EMPTY;
    ModNetworkingClient.sendSettingsRequest(handler.syncId, 0, 0);
  }

  private void drawContainerTooltip(DrawContext context, int mouseX, int mouseY) {
    if (!tab.isSettings()) {
      return;
    }
    final int index = settings.containerRowAt(hoveredWindowX, hoveredWindowY);
    final ContainerRoleEntry entry =
        index == TerminalSettingsLayout.NO_ROW ? null : settingsThisFrame.at(index);
    if (entry == null) {
      return;
    }
    final List<Text> lines = new ArrayList<>();
    lines.add(ContainerRowText.position(entry));
    if (settingsThisFrame.mayAdminister()) {
      lines.add(TerminalScreenText.of("settings.role.hint").formatted(Formatting.GRAY));
    }
    context.drawTooltip(textRenderer, lines, mouseX, mouseY);
  }

  private void drawCellTooltip(DrawContext context, int mouseX, int mouseY) {
    if (hoveredCell == TerminalScreenLayout.NO_CELL) {
      return;
    }
    final ListingRow<ItemVariant> row = items.rowAt(hoveredCell);
    final ItemStack stack = row.variant().toStack();
    final List<Text> lines = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
    lines.addAll(ListingTooltip.extraLines(row));
    context.drawTooltip(textRenderer, lines, mouseX, mouseY);
  }

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }
}
