package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingStatus;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.screenhandler.FilterColumnLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
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
  private final TerminalTagsPanel tagsPanel = new TerminalTagsPanel();
  private final TerminalFilterColumn filters = new TerminalFilterColumn();
  private final FilterSelection selection = new FilterSelection();

  private final TerminalPrompts prompts;
  private final TerminalSettingsClicks settingsClicks;

  private final ListingCriteria requested = new ListingCriteria();

  private TerminalTab tab = TerminalTab.ITEMS;
  private TerminalSearchBox search;
  private TerminalIconButton depositAll;
  private TerminalIconButton rescan;
  private TerminalIconButton highlight;

  private NetworkSettingsView settingsThisFrame = NetworkSettingsView.pending();
  private NetworkTagView tagsThisFrame = NetworkTagView.pending();

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
    this.prompts = new TerminalPrompts(handler.syncId);
    this.settingsClicks = new TerminalSettingsClicks(settings, tagsPanel, prompts.naming());
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
    NetworkTagMirror.opened(handler.syncId);
    TerminalTagRequests.openPanel(handler.syncId);
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
    NetworkTagMirror.closed();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    items.beginFrame(TerminalListingMirror.page());
    settingsThisFrame = NetworkSettingsMirror.view();
    tagsThisFrame = NetworkTagMirror.view();
    settings.fit(settingsThisFrame, tagsThisFrame);
    filters.fit(tagsThisFrame);
    prompts.fit(tagsThisFrame);
    if (tagsThisFrame.answered()) {
      selection.retainAll(knownTagIds());
    }
    if (requested.isNewQuery(search.text())
        || requested.isNewFilter(selection.active(), selection.mode())) {
      items.scroll().scrollTo(0);
    }
    if (!tab.isSettings()
        && requested.needsRequest(
            items.scroll().window(), search.text(), selection.active(), selection.mode())) {
      requestVisibleRows();
    }
    if (tab.isSettings() && !settings.scroll().window().equals(requestedContainers)) {
      requestVisibleContainers();
    }
    updateWidgets(items.page().status());
    updateHovered(mouseX, mouseY);
    super.render(context, mouseX, mouseY, delta);
    if (prompts.draw(
        context,
        textRenderer,
        tagsThisFrame,
        items.page().totalMatching(),
        x,
        y,
        hoveredWindowX,
        hoveredWindowY)) {
      return;
    }
    drawMouseoverTooltip(context, mouseX, mouseY);
    TerminalTooltips.cell(
        context,
        textRenderer,
        client,
        hoveredCell == TerminalScreenLayout.NO_CELL ? null : items.rowAt(hoveredCell),
        tagsThisFrame.mayAdminister(),
        mouseX,
        mouseY);
    if (tab.isSettings()) {
      TerminalTooltips.container(
          context,
          textRenderer,
          settingsThisFrame,
          settings,
          hoveredWindowX,
          hoveredWindowY,
          mouseX,
          mouseY);
    } else {
      TerminalTooltips.filterTag(
          context,
          textRenderer,
          tagsThisFrame,
          filters,
          hoveredWindowX,
          hoveredWindowY,
          mouseX,
          mouseY);
    }
    TerminalTooltips.tab(context, textRenderer, hoveredTab, mouseX, mouseY);
  }

  private void updateHovered(int mouseX, int mouseY) {
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    if (prompts.ownsTheKeyboard()) {
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
      if (settings.section() == SettingsSection.TAGS) {
        tagsPanel.draw(
            context,
            textRenderer,
            tagsThisFrame,
            settings.scroll().firstIndex(),
            0,
            0,
            hoveredWindowX,
            hoveredWindowY);
      }
      return;
    }
    filters.draw(
        context, textRenderer, tagsThisFrame, selection, 0, 0, hoveredWindowX, hoveredWindowY);
    items.drawRows(context, textRenderer);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (prompts.scrolled(verticalAmount)) {
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
    if (filters.scroll().overflows()
        && FilterColumnLayout.isOverColumn(windowX(mouseX), windowY(mouseY))) {
      filters.scroll().scrollBy(notches);
      return true;
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
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    if (prompts.isAnyOpen()) {
      return prompts.mouseClicked(tagsThisFrame, hasShiftDown(), windowX, windowY, button);
    }
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
      final SettingsSection clickedSection = SettingsSection.at(windowX, windowY);
      if (clickedSection != null) {
        showSection(clickedSection);
        return true;
      }
      return settingsClicks.handle(
              handler.syncId,
              settingsThisFrame,
              tagsThisFrame,
              settings.scroll().firstIndex(),
              windowX,
              windowY,
              button)
          || super.mouseClicked(mouseX, mouseY, button);
    }
    if (filters.click(tagsThisFrame, selection, windowX, windowY, button).isHandled()) {
      return true;
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

  private void showSection(SettingsSection shown) {
    if (settings.section() == shown) {
      return;
    }
    tagsPanel.disarm();
    settings.show(shown);
    if (shown == SettingsSection.TAGS) {
      stopAskingForContainers();
      return;
    }
    requestVisibleContainers();
  }

  private boolean handleGridClick(int windowX, int windowY, int button) {
    final int cell = TerminalScreenLayout.cellAt(windowX, windowY);
    if (cell == TerminalScreenLayout.NO_CELL) {
      return false;
    }
    final ListingRow<ItemVariant> row = items.page().status().hasRows() ? items.rowAt(cell) : null;
    if (row != null
        && button == GridClick.LEFT_BUTTON
        && hasControlDown()
        && tagsThisFrame.mayAdminister()) {
      prompts.openTagging(row.variant());
      return true;
    }
    final GridAction action =
        GridClick.of(button, hasShiftDown(), !handler.getCursorStack().isEmpty(), row != null);
    if (action == GridAction.NONE) {
      return false;
    }
    if (action == GridAction.PROMPT_FOR_AMOUNT) {
      prompts.openAmount(row);
      return true;
    }
    TerminalGridRequests.send(action, handler.syncId, row == null ? null : row.variant());
    return true;
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
    depositAll.visible = !prompts.isAnyOpen() && !settingsShowing;
    rescan.visible = !prompts.isAnyOpen() && settingsShowing;
    highlight.visible = rescan.visible;
    rescan.active = settingsThisFrame.powered() && settingsThisFrame.mayAdminister();
    highlight.active = settingsThisFrame.powered();
    search.setVisible(!settingsShowing);
  }

  private boolean isDepositAllKey(int keyCode, int scanCode) {
    return TerminalKeyBindings.isDepositAll(keyCode, scanCode)
        && !tab.isSettings()
        && !prompts.isTagging()
        && !search.isFocused();
  }

  private void depositEverything() {
    ModNetworkingClient.sendDepositAllRequest(handler.syncId);
  }

  private void act(TerminalAction action) {
    ModNetworkingClient.sendTerminalAction(handler.syncId, action);
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (prompts.isAnyOpen()) {
      return true;
    }
    if (settings.isDraggingScrollbar()) {
      settings.dragScrollbarTo(windowY(mouseY));
      return true;
    }
    if (filters.isDraggingScrollbar()) {
      filters.dragScrollbarTo(windowY(mouseY));
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
    if (prompts.keyPressed(keyCode)) {
      return true;
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
    if (prompts.charTyped(typed)) {
      return true;
    }
    return !tab.isSettings() && super.charTyped(typed, modifiers);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GridClick.LEFT_BUTTON && settings.isDraggingScrollbar()) {
      settings.releaseScrollbar();
      return true;
    }
    if (button == GridClick.LEFT_BUTTON && filters.isDraggingScrollbar()) {
      filters.releaseScrollbar();
      return true;
    }
    if (button == GridClick.LEFT_BUTTON && items.scrollbar().isDragging()) {
      items.scrollbar().release();
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private void requestVisibleRows() {
    requested.requested(
        items.scroll().window(), search.text(), selection.active(), selection.mode());
    ModNetworkingClient.sendListingRequest(
        handler.syncId, requested.window(), requested.search(), requested.mode(), requested.tags());
  }

  private Set<String> knownTagIds() {
    final Set<String> known = new HashSet<>();
    for (final NetworkTagEntry tag : tagsThisFrame.tags()) {
      known.add(tag.id());
    }
    return known;
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

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }
}
