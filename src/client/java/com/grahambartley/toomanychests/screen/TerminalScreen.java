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
import org.lwjgl.glfw.GLFW;

public class TerminalScreen extends HandledScreen<TerminalScreenHandler> {
  private final TerminalItemsPanel items = new TerminalItemsPanel();
  private final TerminalSettingsPanel settings = new TerminalSettingsPanel();
  private final TerminalTagsPanel tagsPanel = new TerminalTagsPanel();
  private final TerminalFilterColumn filters = new TerminalFilterColumn();
  private final FilterSelection selection = new FilterSelection();

  private final TerminalPrompts prompts;
  private final TerminalSettingsClicks settingsClicks;

  private final TerminalRequests requests;

  private TerminalTab tab = TerminalTab.ITEMS;
  private TerminalSearchBox search;
  private TerminalIconButton depositAll;
  private TerminalIconButton sortNetwork;
  private TerminalIconButton consolidate;
  private TerminalIconButton rescan;
  private TerminalIconButton highlight;

  private NetworkSettingsView settingsThisFrame = NetworkSettingsView.pending();
  private NetworkTagView tagsThisFrame = NetworkTagView.pending();

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
    this.requests = new TerminalRequests(handler.syncId);
    this.prompts = new TerminalPrompts(handler.syncId);
    this.settingsClicks =
        new TerminalSettingsClicks(settings, tagsPanel, prompts.naming(), prompts.copying());
  }

  @Override
  protected void init() {
    super.init();
    search = new TerminalSearchBox(textRenderer, x, y, search == null ? "" : search.text());
    addDrawableChild(search.widget());
    depositAll = action(0, TerminalSprite.DEPOSIT_ALL, "deposit_all", this::depositEverything);
    sortNetwork =
        action(
            1, TerminalSprite.SORT_NETWORK, "sort_network", () -> act(TerminalAction.SORT_NETWORK));
    consolidate =
        action(2, TerminalSprite.CONSOLIDATE, "consolidate", () -> act(TerminalAction.CONSOLIDATE));
    rescan = action(0, TerminalSprite.RESCAN, "rescan", () -> act(TerminalAction.RESCAN));
    highlight =
        action(1, TerminalSprite.HIGHLIGHT, "highlight", () -> act(TerminalAction.HIGHLIGHT));
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

  private TerminalIconButton action(int row, TerminalSprite icon, String label, Runnable onPress) {
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
  protected void handledScreenTick() {
    super.handledScreenTick();
    TerminalNoticeMirror.tick();
  }

  @Override
  public void removed() {
    super.removed();
    TerminalNoticeMirror.clear();
    TerminalListingMirror.closed();
    NetworkSettingsMirror.closed();
    NetworkTagMirror.closed();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    items.beginFrame(TerminalListingMirror.page());
    settingsThisFrame = NetworkSettingsMirror.view();
    tagsThisFrame = NetworkTagMirror.view();
    settings.fit(settingsThisFrame, tagsThisFrame.custom().size());
    filters.fit(tagsThisFrame);
    prompts.fit(tagsThisFrame);
    if (tagsThisFrame.answered()) {
      selection.retainAll(knownTagIds());
    }
    if (requests.isNewListing(search.text(), selection.active(), selection.mode())) {
      items.scroll().scrollTo(0);
    }
    if (!tab.isSettings()
        && requests.needsRows(
            items.scroll().window(), search.text(), selection.active(), selection.mode())) {
      requestVisibleRows();
    }
    if (tab.isSettings()
        && settings.section() == SettingsSection.CONTAINERS
        && requests.needsContainers(settings.scroll().window())) {
      requestVisibleContainers();
    }
    settings.coverTopRows(tab.isSettings() ? noticeRows() : 0);
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
    drawNotice(context);
    drawMouseoverTooltip(context, mouseX, mouseY);
    TerminalTooltips.cell(
        context,
        textRenderer,
        client,
        hoveredCell == TerminalScreenLayout.NO_CELL ? null : items.rowAt(hoveredCell),
        tagsThisFrame.mayAdminister(),
        mouseX,
        mouseY);
    if (tab.isSettings() && !settings.isCoveredAt(hoveredWindowX, hoveredWindowY)) {
      TerminalTooltips.copyTags(
          context,
          textRenderer,
          tagsThisFrame,
          settings.section(),
          hoveredWindowX,
          hoveredWindowY,
          mouseX,
          mouseY);
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

  private int noticeRows() {
    if (!TerminalNoticeMirror.isShowingOn(handler.syncId)) {
      return 0;
    }
    return TerminalNoticeBanner.rowsCovered(textRenderer, TerminalNoticeMirror.message());
  }

  private void drawNotice(DrawContext context) {
    if (!TerminalNoticeMirror.isShowingOn(handler.syncId)) {
      return;
    }
    TerminalNoticeBanner.draw(
        context,
        textRenderer,
        TerminalNoticeMirror.message(),
        TerminalNoticeMirror.needsAnAnswer(),
        x,
        tab.isSettings() ? y + TerminalNoticeBanner.settingsDrop() : y);
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
    TerminalSprite.PANEL.draw(context, x, y, backgroundWidth, backgroundHeight);
    if (!tab.isSettings()) {
      search.drawBox(context, x, y);
    }
    TerminalSprite.SUNKEN.draw(
        context,
        x + TerminalScreenLayout.INSET_X,
        y + TerminalScreenLayout.INSET_Y,
        TerminalScreenLayout.INSET_WIDTH,
        TerminalScreenLayout.INSET_HEIGHT);
    if (!tab.isSettings()) {
      items.drawCells(context, x, y);
      items.drawScrollbar(context, x, y);
    }
    drawTabs(context);
    for (final Slot slot : handler.slots) {
      TerminalSprite.SUNKEN.draw(
          context,
          x + slot.x - TerminalScreenLayout.SLOT_ITEM_OFFSET,
          y + slot.y - TerminalScreenLayout.SLOT_ITEM_OFFSET,
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  private void drawTabs(DrawContext context) {
    drawTab(context, TerminalTab.ITEMS, TerminalSprite.ITEMS);
    drawTab(context, TerminalTab.SETTINGS, TerminalSprite.SETTINGS);
  }

  private void drawTab(DrawContext context, TerminalTab drawn, TerminalSprite icon) {
    final int left = x + TerminalScreenLayout.TOOLBAR_X;
    final int top = y + TerminalScreenLayout.tabY(drawn.index());
    TerminalChrome.tab(drawn == tab, drawn == hoveredTab)
        .draw(
            context, left, top, TerminalScreenLayout.BUTTON_SIZE, TerminalScreenLayout.BUTTON_SIZE);
    icon.draw(
        context,
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
            noticeRows(),
            0,
            0,
            hoveredWindowX,
            hoveredWindowY);
      }
      return;
    }
    filters.draw(
        context, textRenderer, tagsThisFrame, selection, 0, 0, hoveredWindowX, hoveredWindowY);
    items.drawRows(context, textRenderer, hoveredCell, noticeRows());
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
    if (button == GridClick.LEFT_BUTTON && TerminalScreenLayout.isOverSearch(windowX, windowY)) {
      setFocused(search.widget());
      search.widget().mouseClicked(mouseX, mouseY, button);
      return true;
    }
    releaseTheSearchBox();
    if (filters.click(tagsThisFrame, selection, windowX, windowY, button)) {
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

  private void releaseTheSearchBox() {
    if (search.isFocused()) {
      setFocused(null);
    }
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
    requests.hideSettings();
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
    final boolean online = status != ListingStatus.OFFLINE;
    depositAll.active = online;
    depositAll.visible = !prompts.isAnyOpen() && !settingsShowing;
    sortNetwork.visible = depositAll.visible;
    consolidate.visible = depositAll.visible;
    sortNetwork.active = online && tagsThisFrame.mayAdminister();
    consolidate.active = sortNetwork.active;
    rescan.visible = !prompts.isAnyOpen() && settingsShowing;
    highlight.visible = rescan.visible;
    rescan.active =
        settingsThisFrame.answered()
            && settingsThisFrame.powered()
            && settingsThisFrame.mayAdminister();
    highlight.active = settingsThisFrame.answered() && settingsThisFrame.powered();
    search.setVisible(!settingsShowing);
  }

  private boolean isDepositAllKey(int keyCode, int scanCode) {
    return DepositAllKey.deposits(
        ModKeyBindings.isDepositAll(keyCode, scanCode),
        tab.isSettings(),
        prompts.isTagging(),
        search.isFocused());
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
    requests.rows(items.scroll().window(), search.text(), selection.active(), selection.mode());
  }

  private Set<String> knownTagIds() {
    final Set<String> known = new HashSet<>();
    for (final NetworkTagEntry tag : tagsThisFrame.tags()) {
      known.add(tag.id());
    }
    return known;
  }

  private void requestVisibleContainers() {
    requests.showSettings(
        settings.section() == SettingsSection.CONTAINERS
            ? settings.scroll().window()
            : ListingWindow.EMPTY);
  }

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }
}
