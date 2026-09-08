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

/**
 * The terminal's window: its chassis, whichever of its two views is showing, and the player's own
 * inventory.
 *
 * <p>Each view owns its own recess full of content, so this class owns the frame around them: the
 * chassis, the tab strip, the toolbar, and deciding which view a click was meant for. See {@link
 * TerminalItemsPanel} and {@link TerminalSettingsPanel} for the views themselves.
 *
 * <p>Both views are drawn into the same recess because the window is already as tall as vanilla
 * will draw one on a 320x240 screen, so there is no room for a second. That is also why the tabs
 * sit in the toolbar column rather than above the window, where vanilla puts them.
 *
 * <p>Every piece of chassis is a nine-sliced sprite rather than one background image, per ADR 0017,
 * so the window can change size as the filter column grows without a new texture.
 *
 * <p>The search box takes focus as the screen opens and keeps it, so typing narrows the grid
 * without a click first. That is also why this screen has to say what a key press means rather than
 * leaving it to its parent: see {@link SearchKeyOutcome}.
 */
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

  /** What the server was last told this screen wants, so a scroll or a keystroke is noticed. */
  private final ListingCriteria requested = new ListingCriteria();

  private TerminalTab tab = TerminalTab.ITEMS;
  private TerminalSearchBox search;
  private TerminalIconButton depositAll;
  private TerminalIconButton rescan;
  private TerminalIconButton highlight;

  private NetworkSettingsView settingsThisFrame = NetworkSettingsView.pending();

  /**
   * What the settings panel last asked for, so a wheel spun hard costs one request rather than ten.
   */
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

  /**
   * Opening the screen asks for the first page. Nothing arrives unprompted: the server answers the
   * window a screen has told it about, so a screen that never asks is never sent anything.
   *
   * <p>Laying the window out again is not opening it. Minecraft runs this on every resize and every
   * change of GUI scale, and treating those as a fresh screen would throw away the page being shown
   * and ask for the first one again, wherever the player had scrolled to. The search box is built
   * again either way, because a resize clears the widgets, and it is handed back what was typed
   * rather than reopened empty.
   */
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

  /**
   * Scrolling asks the server for the rows the grid is now showing, once a frame rather than once a
   * notch. A wheel spun hard is several notches in one frame and one window at the end of them, so
   * the requests a player generates are bounded by their frame rate rather than by their wrist.
   */
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

  /** The tabs are two glyphs, so what each one opens has to be readable somewhere. */
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

  /**
   * A click anywhere on the search box reaches the field, which covers only the text inside it. The
   * field still sees the click, so one landing on the text puts the cursor where it was aimed
   * rather than merely taking focus.
   */
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

  /** Switching view asks for whatever the new one draws, since neither is sent unprompted. */
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

  /**
   * A click over the grid, decided by {@link GridClick} rather than by vanilla's slot handling. The
   * grid holds no slots per ADR 0025, so every button and modifier over it means what this screen
   * says it means and nothing is moved until the server answers.
   */
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

  /**
   * A click on the settings view, which has four things it could have landed on: a section tab, a
   * stepper, a setting that cycles, or a container row. Each is asked in the order it is drawn.
   *
   * <p>Nothing is applied here. Every control sends what the player asked for and waits, because
   * per ADR 0008 whether they may is the server's answer and this end only knows what it was last
   * told.
   */
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

  /** A setting a click can change, which today is the access mode and nothing else. */
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

  /** Switching section resets the scroll, so a list is never opened part way down. */
  private void showSection(SettingsSection shown) {
    if (settings.section() != shown) {
      settings.show(shown);
      requestVisibleContainers();
    }
  }

  /**
   * The radius steps by one. The two steppers carry their direction in the glyph they wear, so
   * neither needs a cycle and a right click on one means nothing.
   */
  private void stepRadius(SettingsControl control) {
    ModNetworkingClient.sendRadiusEdit(
        handler.syncId,
        settingsThisFrame.radius() + (control == SettingsControl.RADIUS_UP ? 1 : -1));
  }

  /**
   * Shift-clicking a stack out of the player's own inventory hands it to the network, which is what
   * a player expects the gesture to do in front of a container. It becomes a request rather than a
   * vanilla quick move, because there is no slot on this screen for the stack to move into.
   */
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

  /**
   * Each view brings its own actions. Deposit all belongs to the items view because it is about
   * what the network holds; rescan and highlight belong to the settings view because they are about
   * what it reaches.
   */
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

  /**
   * Whether this press is the deposit-all key, which a focused search box outranks.
   *
   * <p>A key bound to a letter is a letter first while somebody is typing one, which is how vanilla
   * treats its own inventory key inside the creative search field. Yielding is what stops searching
   * for "redstone" from emptying an inventory on the r.
   */
  private boolean isDepositAllKey(int keyCode, int scanCode) {
    return TerminalKeyBindings.isDepositAll(keyCode, scanCode) && !search.isFocused();
  }

  private void depositEverything() {
    ModNetworkingClient.sendDepositAllRequest(handler.syncId);
  }

  private void act(TerminalAction action) {
    ModNetworkingClient.sendTerminalAction(handler.syncId, action);
  }

  /**
   * The most a custom amount could really deliver, which is the smaller of what the network holds
   * and what the player's own slots could carry. Offering the network's total instead would print a
   * number the server is about to clamp, which is the one thing the prompt promises not to do.
   */
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

  /**
   * A key press while the search box has focus is the player typing into it, so the screen behind
   * it never sees one. Without this the inventory key would close the terminal rather than write a
   * letter, and a digit would swap a hotbar slot rather than be typed.
   */
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

  /** The prompt swallows every key while it is up, so nothing behind it hears the number. */
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

  /**
   * Leaving the settings view tells the server, so an edit by somebody else stops pushing panels to
   * a screen that has gone back to the grid. A window of no rows is what closes it.
   */
  private void stopAskingForContainers() {
    requestedContainers = ListingWindow.EMPTY;
    ModNetworkingClient.sendSettingsRequest(handler.syncId, 0, 0);
  }

  /**
   * Where a container is, and what clicking it does.
   *
   * <p>A row leads with what is inside it because that is what picks it out of a list, so its
   * coordinates go here: wanted once, when a player has found the row and now has to walk to the
   * chest. The hint is only shown to somebody the change would work for.
   */
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
