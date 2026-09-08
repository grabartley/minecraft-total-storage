package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingStatus;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * The terminal's window: its chassis, the grid of everything the network holds, and the player's
 * own inventory.
 *
 * <p>The grid is not made of slots and cannot be. Per ADR 0025 the network is not an inventory, so
 * a cell is drawn from a row of the page the server last sent rather than from anything the handler
 * addresses, and a cell holds a variant and a total rather than a stack.
 *
 * <p>Cells are addressed by their position in the whole listing rather than by their position in
 * the page in hand. A scroll and the page answering it are a round trip apart, and drawing by page
 * position would slide every cell sideways for that round trip instead of leaving the rows that
 * still overlap exactly where they were.
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
  private static final Identifier CELL = TerminalSprites.of("slot/cell");
  private static final Identifier CELL_HOVER = TerminalSprites.of("slot/cell_hover");
  private static final Identifier SCROLL_TRACK = TerminalSprites.of("scroll/track");
  private static final Identifier SCROLL_THUMB = TerminalSprites.of("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED =
      TerminalSprites.of("scroll/thumb_disabled");

  private static final int ITEM_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  /** Far enough forward to clear the item model the count is written across. */
  private static final float COUNT_ELEVATION = 200.0F;

  private static final int COUNT_COLOUR = 0xFFFFFF;

  /** The palette's mid glyph, which is what reads on the recess floor behind an empty grid. */
  private static final int PLACEHOLDER_COLOUR = 0x9AA3B3;

  private static final Identifier DEPOSIT_ALL_ICON = TerminalSprites.of("icon/deposit_all");

  private final ListingScroll scroll =
      new ListingScroll(TerminalScreenLayout.LISTING_COLUMNS, TerminalScreenLayout.LISTING_ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TerminalScreenLayout.SCROLLBAR_HEIGHT);

  /**
   * The page every part of one frame draws from, taken once at the top of it. Reading the mirror
   * again partway through would let the chassis, the cells and the tooltip disagree about what is
   * on screen.
   */
  private ListingPage<ItemVariant> pageThisFrame = ListingPage.pending(0L);

  /** What the grid is showing instead of rows, taken with the page so the two cannot disagree. */
  private Optional<ListingPlaceholder> placeholder = Optional.empty();

  private TerminalSearchBox search;

  private TerminalIconButton depositAll;

  private final WithdrawAmountPrompt prompt = new WithdrawAmountPrompt();

  /** What the server was last told this screen wants, so a scroll or a keystroke is noticed. */
  private final ListingCriteria requested = new ListingCriteria();

  private int hovered = TerminalScreenLayout.NO_CELL;

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
    depositAll =
        new TerminalIconButton(
            x + TerminalScreenLayout.TOOLBAR_X,
            y + TerminalScreenLayout.TOOLBAR_Y,
            DEPOSIT_ALL_ICON,
            TerminalScreenText.of("deposit_all"),
            this::depositEverything);
    addDrawableChild(depositAll);
    setInitialFocus(search.widget());
    updateToolbar(TerminalListingMirror.page().status());
    if (TerminalListingMirror.isShowing(handler.syncId)) {
      return;
    }
    TerminalListingMirror.opened(handler.syncId);
    requestVisibleRows();
  }

  @Override
  public void removed() {
    super.removed();
    TerminalListingMirror.closed();
  }

  /**
   * Scrolling asks the server for the rows the grid is now showing, once a frame rather than once a
   * notch. A wheel spun hard is several notches in one frame and one window at the end of them, so
   * the requests a player generates are bounded by their frame rate rather than by their wrist.
   */
  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    pageThisFrame = TerminalListingMirror.page();
    placeholder = ListingPlaceholder.forStatus(pageThisFrame.status());
    if (requested.isNewQuery(search.text())) {
      scroll.scrollTo(0);
    }
    scroll.fit(pageThisFrame.totalMatching());
    if (requested.needsRequest(scroll.window(), search.text())) {
      requestVisibleRows();
    }
    updateToolbar(pageThisFrame.status());
    hovered = prompt.isOpen() ? TerminalScreenLayout.NO_CELL : hoveredCell(mouseX, mouseY);
    super.render(context, mouseX, mouseY, delta);
    if (prompt.isOpen()) {
      prompt.draw(context, textRenderer, x, y);
      return;
    }
    drawMouseoverTooltip(context, mouseX, mouseY);
    drawCellTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    context.drawGuiTexture(PANEL, x, y, backgroundWidth, backgroundHeight);
    search.drawBox(context, x, y);
    context.drawGuiTexture(
        INSET,
        x + TerminalScreenLayout.INSET_X,
        y + TerminalScreenLayout.INSET_Y,
        TerminalScreenLayout.INSET_WIDTH,
        TerminalScreenLayout.INSET_HEIGHT);
    drawCells(context);
    drawScrollbar(context);
    for (final Slot slot : handler.slots) {
      context.drawGuiTexture(
          INSET,
          x + slot.x - 1,
          y + slot.y - 1,
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    super.drawForeground(context, mouseX, mouseY);
    placeholder.ifPresent(shown -> drawPlaceholder(context, shown));
    drawRows(context);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (prompt.isOpen()) {
      return true;
    }
    if (verticalAmount != 0.0
        && scroll.overflows()
        && TerminalScreenLayout.isOverListing(windowX(mouseX), windowY(mouseY))) {
      scroll.scrollBy(verticalAmount > 0.0 ? -1 : 1);
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
    if (button == GridClick.LEFT_BUTTON
        && TerminalScreenLayout.isOverSearch(windowX(mouseX), windowY(mouseY))) {
      setFocused(search.widget());
      search.widget().mouseClicked(mouseX, mouseY, button);
      return true;
    }
    if (button == GridClick.LEFT_BUTTON
        && TerminalScreenLayout.isOverScrollbar(windowX(mouseX), windowY(mouseY))
        && scrollbar.grab(windowY(mouseY) - TerminalScreenLayout.SCROLLBAR_Y)) {
      return true;
    }
    if (handleGridClick(mouseX, mouseY, button)) {
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  /**
   * A click over the grid, decided by {@link GridClick} rather than by vanilla's slot handling. The
   * grid holds no slots per ADR 0025, so every button and modifier over it means what this screen
   * says it means and nothing is moved until the server answers.
   */
  private boolean handleGridClick(double mouseX, double mouseY, int button) {
    final int cell = TerminalScreenLayout.cellAt(windowX(mouseX), windowY(mouseY));
    if (cell == TerminalScreenLayout.NO_CELL) {
      return false;
    }
    final ListingRow<ItemVariant> row = pageThisFrame.status().hasRows() ? rowAt(cell) : null;
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
   * The button says what the terminal can currently do. It steps aside entirely for the amount
   * prompt, because a widget drawn over a modal panel would take its tooltip with it.
   */
  private void updateToolbar(ListingStatus status) {
    depositAll.active = status != ListingStatus.OFFLINE;
    depositAll.visible = !prompt.isOpen();
  }

  private void depositEverything() {
    ModNetworkingClient.sendDepositAllRequest(handler.syncId);
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
    if (scrollbar.isDragging()) {
      scrollbar.dragTo(windowY(mouseY) - TerminalScreenLayout.SCROLLBAR_Y);
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
    return super.charTyped(typed, modifiers);
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
    if (button == GridClick.LEFT_BUTTON && scrollbar.isDragging()) {
      scrollbar.release();
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private void requestVisibleRows() {
    requested.requested(scroll.window(), search.text());
    ModNetworkingClient.sendListingRequest(
        handler.syncId,
        requested.window(),
        requested.search(),
        TagCombinationMode.DEFAULT,
        Set.of());
  }

  /**
   * A grid explaining why it has no rows draws no cells. The empty-state art was drawn against the
   * recess floor, and forty-five cells behind it leave the illustration showing through the gutters
   * between them rather than reading as a picture.
   */
  private void drawCells(DrawContext context) {
    if (placeholder.isPresent()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      context.drawGuiTexture(
          cell == hovered ? CELL_HOVER : CELL,
          x + TerminalScreenLayout.cellX(cell),
          y + TerminalScreenLayout.cellY(cell),
          TerminalScreenLayout.CELL_SIZE,
          TerminalScreenLayout.CELL_SIZE);
    }
  }

  private void drawScrollbar(DrawContext context) {
    context.drawGuiTexture(
        SCROLL_TRACK,
        x + TerminalScreenLayout.SCROLLBAR_X,
        y + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
    context.drawGuiTexture(
        scrollbar.isEnabled() ? SCROLL_THUMB : SCROLL_THUMB_DISABLED,
        x + TerminalScreenLayout.SCROLLBAR_X,
        y + TerminalScreenLayout.SCROLLBAR_Y + scrollbar.thumbTop(),
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        scrollbar.thumbHeight());
  }

  private void drawRows(DrawContext context) {
    if (!pageThisFrame.status().hasRows()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      final ListingRow<ItemVariant> row = rowAt(cell);
      if (row == null) {
        continue;
      }
      final int itemX = TerminalScreenLayout.cellX(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      final int itemY = TerminalScreenLayout.cellY(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      context.drawItem(row.variant().toStack(), itemX, itemY);
      drawCount(context, ItemCountFormat.cell(row.count()), itemX, itemY);
    }
  }

  /**
   * The count sits in the item's bottom right corner as a stack size does, shrunk only as far as
   * the cell demands. A network total can be several times wider than the two digits vanilla ever
   * has to fit there, but most cells hold a count that fits at full size, and shrinking those too
   * turns the common case to mush at the smallest GUI scale for the sake of the rare one.
   */
  private void drawCount(DrawContext context, String label, int itemX, int itemY) {
    final int width = textRenderer.getWidth(label);
    final float scale = Math.min(1.0F, (float) ITEM_SIZE / width);
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, COUNT_ELEVATION);
    context.getMatrices().scale(scale, scale, 1.0F);
    context.drawText(
        textRenderer,
        label,
        Math.round((itemX + ITEM_SIZE) / scale) - width,
        Math.round((itemY + ITEM_SIZE - LINE_HEIGHT * scale) / scale),
        COUNT_COLOUR,
        true);
    context.getMatrices().pop();
  }

  /**
   * The message is wrapped to the grid rather than trusted to fit it. English happens to be short
   * enough, but a translation is not, and one long line runs off both sides of the chassis and off
   * the screen with it.
   */
  private void drawPlaceholder(DrawContext context, ListingPlaceholder shown) {
    final int centreX = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH / 2;
    final List<OrderedText> lines =
        textRenderer.wrapLines(shown.message(), TerminalScreenLayout.GRID_WIDTH);
    final int block =
        ListingPlaceholder.SPRITE_SIZE
            + ListingPlaceholder.MESSAGE_GAP
            + lines.size() * LINE_HEIGHT;
    final int top =
        Math.max(
            TerminalScreenLayout.GRID_Y,
            TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - block) / 2);
    context.drawGuiTexture(
        shown.sprite(),
        centreX - ListingPlaceholder.SPRITE_SIZE / 2,
        top,
        ListingPlaceholder.SPRITE_SIZE,
        ListingPlaceholder.SPRITE_SIZE);
    int lineY = top + ListingPlaceholder.SPRITE_SIZE + ListingPlaceholder.MESSAGE_GAP;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          centreX - textRenderer.getWidth(line) / 2,
          lineY,
          PLACEHOLDER_COLOUR,
          false);
      lineY += LINE_HEIGHT;
    }
  }

  private void drawCellTooltip(DrawContext context, int mouseX, int mouseY) {
    if (hovered == TerminalScreenLayout.NO_CELL) {
      return;
    }
    final ListingRow<ItemVariant> row = rowAt(hovered);
    final ItemStack stack = row.variant().toStack();
    final List<Text> lines = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
    lines.addAll(ListingTooltip.extraLines(row));
    context.drawTooltip(textRenderer, lines, mouseX, mouseY);
  }

  /** The cell under the pointer, or {@link TerminalScreenLayout#NO_CELL} when it holds no row. */
  private int hoveredCell(int mouseX, int mouseY) {
    if (!pageThisFrame.status().hasRows()) {
      return TerminalScreenLayout.NO_CELL;
    }
    final int cell = TerminalScreenLayout.cellAt(windowX(mouseX), windowY(mouseY));
    if (cell == TerminalScreenLayout.NO_CELL || rowAt(cell) == null) {
      return TerminalScreenLayout.NO_CELL;
    }
    return cell;
  }

  private ListingRow<ItemVariant> rowAt(int cell) {
    return pageThisFrame.at(scroll.firstIndex() + cell);
  }

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }
}
