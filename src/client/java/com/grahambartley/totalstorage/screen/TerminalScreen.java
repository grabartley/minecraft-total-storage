package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import com.grahambartley.totalstorage.listing.ListingRow;
import com.grahambartley.totalstorage.listing.ListingStatus;
import com.grahambartley.totalstorage.listing.TerminalListingMirror;
import com.grahambartley.totalstorage.network.DepositRequestPayload;
import com.grahambartley.totalstorage.network.ModNetworkingClient;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
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
  private final TerminalScrollbars scrollbars;

  private final TerminalRequests requests;

  private TerminalTab tab = TerminalTab.ITEMS;
  private TerminalSearchBox search;
  private FuzzySearchButton fuzzy;
  private TerminalActionDock dock;
  private TerminalSettingsButtons settingsButtons;

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
    this.settingsClicks = new TerminalSettingsClicks(settings, tagsPanel, prompts);
    this.scrollbars = new TerminalScrollbars(prompts, settings, filters, items);
  }

  @Override
  protected void init() {
    super.init();
    search = new TerminalSearchBox(textRenderer, x, y, search == null ? "" : search.text());
    addDrawableChild(search.widget());
    fuzzy = new FuzzySearchButton(x, y, fuzzySearch(), ModNetworkingClient::sendFuzzySearch);
    addDrawableChild(fuzzy);
    filters.attach(this::addDrawableChild, x, y, selection);
    dock =
        new TerminalActionDock(
            this::addDrawableChild, x, y, new TerminalDockActions(handler.syncId, prompts));
    settingsButtons =
        new TerminalSettingsButtons(
            this::addDrawableChild, x, y, new TerminalSettingsEditSender(handler.syncId, prompts));
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
    requestSettings();
  }

  @Override
  protected void handledScreenTick() {
    super.handledScreenTick();
    TerminalNoticeMirror.tick();
    TerminalOperationMirror.tick();
    prompts.tick();
  }

  @Override
  public void removed() {
    super.removed();
    TerminalNoticeMirror.clear();
    TerminalOperationMirror.clear();
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
    prompts.fit(settingsThisFrame, tagsThisFrame);
    if (tagsThisFrame.answered()) {
      selection.retainAll(tagsThisFrame.ids());
    }
    if (requests.isNewListing(search.text(), selection.active(), selection.mode())) {
      items.scroll().scrollTo(0);
    }
    if (!tab.isSettings()
        && requests.needsRows(
            items.prefetch().needed(), search.text(), selection.active(), selection.mode())) {
      requestVisibleRows();
    }
    if (requests.needsContainers(settings.containersToKeep(tab.isSettings()))) {
      requestSettings();
    }
    updateWidgets(items.page().status());
    updateHovered(mouseX, mouseY);
    super.render(context, mouseX, mouseY, delta);
    if (prompts.draw(
        context,
        textRenderer,
        settingsThisFrame,
        tagsThisFrame,
        items.page().totalMatching(),
        x,
        y,
        windowX(mouseX),
        windowY(mouseY))) {
      TerminalTooltips.text(
          context,
          textRenderer,
          prompts.refusalTooltipAt(windowX(mouseX), windowY(mouseY)),
          mouseX,
          mouseY);
      return;
    }
    drawMouseoverTooltip(context, mouseX, mouseY);
    drawTooltips(context, mouseX, mouseY);
  }

  private void drawTooltips(DrawContext context, int mouseX, int mouseY) {
    TerminalTooltips.tab(context, textRenderer, hoveredTab, mouseX, mouseY);
    if (isShowingNotice()
        && TerminalScreenLayout.isOverStatus(hoveredWindowX, hoveredWindowY)
        && TerminalStatusLine.needsTooltip(
            textRenderer, TerminalNoticeMirror.headline(), TerminalNoticeMirror.message())) {
      TerminalStatusLine.drawTooltip(
          context, textRenderer, TerminalNoticeMirror.message(), mouseX, mouseY);
      return;
    }
    if (TerminalActionDock.isOverGauge(hoveredWindowX, hoveredWindowY)) {
      TerminalTooltips.text(
          context,
          textRenderer,
          TerminalActionDock.gaugeTooltip(settingsThisFrame),
          mouseX,
          mouseY);
      return;
    }
    if (!tab.isSettings()) {
      TerminalTooltips.cell(
          context,
          textRenderer,
          client,
          hoveredCell == TerminalScreenLayout.NO_CELL ? null : items.rowAt(hoveredCell),
          tagsThisFrame.mayAdminister(),
          mouseX,
          mouseY);
      TerminalTooltips.filterTag(
          context, textRenderer, filters, hoveredWindowX, hoveredWindowY, mouseX, mouseY);
      return;
    }
    switch (settings.section()) {
      case TERMINAL ->
          TerminalTooltips.text(
              context,
              textRenderer,
              settings.descriptionTooltipAt(
                  textRenderer, settingsThisFrame, hoveredWindowX, hoveredWindowY),
              mouseX,
              mouseY);
      case CONTAINERS ->
          TerminalTooltips.container(
              context,
              textRenderer,
              settingsThisFrame,
              tagsThisFrame,
              settings,
              hoveredWindowX,
              hoveredWindowY,
              mouseX,
              mouseY);
      case TAGS ->
          TerminalTooltips.text(
              context,
              textRenderer,
              tagsPanel.tooltipAt(
                  tagsThisFrame, settings.scroll().firstIndex(), hoveredWindowX, hoveredWindowY),
              mouseX,
              mouseY);
    }
  }

  private boolean isShowingNotice() {
    return TerminalNoticeMirror.isShowingOn(handler.syncId);
  }

  private void updateHovered(int mouseX, int mouseY) {
    if (prompts.isAnyOpen()) {
      hoveredTab = null;
      hoveredCell = TerminalScreenLayout.NO_CELL;
      hoveredWindowX = Integer.MIN_VALUE;
      hoveredWindowY = Integer.MIN_VALUE;
      return;
    }
    hoveredWindowX = windowX(mouseX);
    hoveredWindowY = windowY(mouseY);
    hoveredTab = TerminalSideTabs.at(hoveredWindowX, hoveredWindowY);
    hoveredCell =
        tab.isSettings()
            ? TerminalScreenLayout.NO_CELL
            : items.hoveredCell(hoveredWindowX, hoveredWindowY);
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    TerminalSideTabs.draw(context, tab, hoveredTab, x, y);
    TerminalSprite.PANEL.draw(context, x, y, backgroundWidth, backgroundHeight);
    TerminalSideTabs.drawSelected(context, tab, x, y);
    if (tab.isSettings()) {
      settings.drawBackground(context, x, y, hoveredWindowX, hoveredWindowY);
      if (settings.section() == SettingsSection.CONTAINERS) {
        ContainerListView.drawBackground(
            context, settingsThisFrame, settings.scroll().firstIndex(), x, y);
      }
    } else {
      search.drawBox(context, x, y);
      items.drawCells(context, x, y);
      items.drawScrollbar(context, x, y);
    }
    for (final Slot slot : handler.slots) {
      VanillaSprite.SLOT.draw(
          context,
          x + slot.x - TerminalScreenLayout.SLOT_ITEM_OFFSET,
          y + slot.y - TerminalScreenLayout.SLOT_ITEM_OFFSET,
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    context.drawText(
        textRenderer, title, titleX, titleY, VanillaPalette.opaque(VanillaPalette.LABEL), false);
    if (isShowingNotice()) {
      TerminalStatusLine.draw(
          context, textRenderer, TerminalNoticeMirror.tone(), TerminalNoticeMirror.headline());
    } else {
      context.drawText(
          textRenderer,
          playerInventoryTitle,
          playerInventoryTitleX,
          playerInventoryTitleY,
          VanillaPalette.opaque(VanillaPalette.LABEL),
          false);
    }
    dock.drawForeground(context, textRenderer, settingsThisFrame);
    if (!tab.isSettings()) {
      filters.draw(context, textRenderer, selection, hoveredWindowX, hoveredWindowY);
      items.drawRows(context, textRenderer, hoveredCell);
      return;
    }
    settings.drawForeground(context, textRenderer, settingsThisFrame);
    switch (settings.section()) {
      case CONTAINERS ->
          ContainerListView.draw(
              context,
              textRenderer,
              settingsThisFrame,
              settings.scroll().firstIndex(),
              settings.containerRowAt(hoveredWindowX, hoveredWindowY),
              ContainerListView.isOverRole(hoveredWindowX));
      case TAGS ->
          tagsPanel.draw(
              context,
              textRenderer,
              tagsThisFrame,
              settings.scroll().firstIndex(),
              hoveredWindowX,
              hoveredWindowY);
      case TERMINAL -> {}
    }
  }

  @Override
  protected boolean isClickOutsideBounds(
      double mouseX, double mouseY, int left, int top, int button) {
    return super.isClickOutsideBounds(mouseX, mouseY, left, top, button)
        && TerminalSideTabs.at(windowX(mouseX), windowY(mouseY)) == null;
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
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    final int listRows = ScrollNotches.rowsFor(verticalAmount);
    if (tab.isSettings()) {
      if (settings.isOverList(windowX, windowY)) {
        settings.scroll().scrollBy(listRows);
        return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    if (filters.scroll().overflows() && FilterColumnLayout.isOverColumn(windowX, windowY)) {
      filters.scroll().scrollBy(listRows);
      return true;
    }
    if (items.scroll().overflows() && TerminalScreenLayout.isOverListing(windowX, windowY)) {
      items.scroll().scrollBy(ScrollNotches.gridRowsFor(verticalAmount));
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    if (prompts.isAnyOpen()) {
      return prompts.mouseClicked(
          settingsThisFrame, tagsThisFrame, hasShiftDown(), windowX, windowY, button);
    }
    final TerminalTab clicked = TerminalSideTabs.at(windowX, windowY);
    if (clicked != null) {
      showTab(clicked);
      return true;
    }
    if (tab.isSettings()) {
      return settingsClicked(mouseX, mouseY, windowX, windowY, button);
    }
    if (button == GridClick.LEFT_BUTTON && TerminalScreenLayout.isOverSearch(windowX, windowY)) {
      setFocused(search.widget());
      search.widget().mouseClicked(mouseX, mouseY, button);
      return true;
    }
    if (fuzzy.takesClick(mouseX, mouseY, button)) {
      fuzzy.playDownSound(client.getSoundManager());
      fuzzy.onClick(mouseX, mouseY);
      return true;
    }
    releaseTheSearchBox();
    if (filters.click(selection, windowX, windowY, button)) {
      return true;
    }
    if (button == GridClick.LEFT_BUTTON
        && TerminalScreenLayout.isOverScrollbar(windowX, windowY)
        && items.grabScrollbar(windowY)) {
      return true;
    }
    if (handleGridClick(windowX, windowY, button)) {
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private boolean settingsClicked(
      double mouseX, double mouseY, int windowX, int windowY, int button) {
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
            hasShiftDown(),
            windowX,
            windowY,
            button)
        || super.mouseClicked(mouseX, mouseY, button);
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
    tagsPanel.disarm();
    requestSettings();
    if (tab.isSettings()) {
      releaseTheSearchBox();
      return;
    }
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
    requestSettings();
  }

  private boolean handleGridClick(int windowX, int windowY, int button) {
    final int cell = TerminalScreenLayout.cellAt(windowX, windowY);
    if (cell == TerminalScreenLayout.NO_CELL) {
      return false;
    }
    final ListingRow<ItemVariant> row = items.page().status().hasRows() ? items.rowAt(cell) : null;
    final GridAction action =
        GridClick.of(
            button,
            hasShiftDown(),
            tagsThisFrame.mayAdminister() && isTagModifierDown(),
            !handler.getCursorStack().isEmpty(),
            row != null);
    switch (action) {
      case NONE -> {
        return false;
      }
      case OPEN_TAGGING -> prompts.openTagging(row.variant());
      case PROMPT_FOR_AMOUNT -> prompts.openAmount(row);
      default -> TerminalGridRequests.send(action, handler.syncId, row);
    }
    return true;
  }

  private boolean isTagModifierDown() {
    final long window = client.getWindow().getHandle();
    return hasControlDown()
        || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL)
        || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
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

  private boolean fuzzySearch() {
    return FuzzySearchAttachment.get(client == null ? null : client.player);
  }

  private void updateWidgets(ListingStatus status) {
    final boolean promptOpen = prompts.isAnyOpen();
    search.setVisible(!tab.isSettings());
    filters.update(!tab.isSettings() && !promptOpen, selection);
    fuzzy.update(!tab.isSettings() && !promptOpen, fuzzySearch());
    settingsButtons.update(
        settingsThisFrame,
        !promptOpen && tab.isSettings() && settings.section() == SettingsSection.TERMINAL);
    dock.update(
        new TerminalActionDock.State(
            tab,
            settings.section(),
            promptOpen,
            status != ListingStatus.OFFLINE,
            tagsThisFrame.mayAdminister(),
            settingsThisFrame.answered() && settingsThisFrame.powered(),
            settingsThisFrame.mayAdminister(),
            tagsThisFrame.mayAdminister(),
            NetworkActionRow.of(
                TerminalOperationMirror.phaseOf(handler.syncId, ExpensiveOperation.SORT_NETWORK),
                TerminalOperationMirror.phaseOf(handler.syncId, ExpensiveOperation.CONSOLIDATE),
                TerminalOperationMirror.percentDone())));
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

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (scrollbars.dragTo(windowY(mouseY)) || prompts.isAnyOpen()) {
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (prompts.keyPressed(keyCode, modifiers, tagsThisFrame)) {
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
    if (button == GridClick.LEFT_BUTTON && scrollbars.release()) {
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private void requestVisibleRows() {
    requests.rows(items.prefetch().window(), search.text(), selection.active(), selection.mode());
  }

  private void requestSettings() {
    requests.showSettings(settings.containersToAskFor(tab.isSettings()));
  }

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }
}
