package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class TerminalActionDock {
  public static final int X = TerminalScreenLayout.SIDE_X;

  public static final int WIDTH = TerminalScreenLayout.SIDE_WIDTH;

  public static final int LABEL_Y = TerminalScreenLayout.LABEL_Y;

  public static final int GAUGE_Y = TerminalScreenLayout.MAIN_INVENTORY_Y - 1;

  public static final int BUTTON_HEIGHT = 20;

  public static final int SECOND_ROW_Y = TerminalScreenLayout.BOTTOM_Y - BUTTON_HEIGHT;

  public static final int ROW_GAP = 9;

  public static final int FIRST_ROW_Y = SECOND_ROW_Y - ROW_GAP - BUTTON_HEIGHT;

  public static final int BUTTON_GAP = 2;

  public static final int HALF_WIDTH = (WIDTH - BUTTON_GAP) / 2;

  public interface Actions {
    void depositAll();

    void network(ExpensiveOperation operation);

    void rescan();

    void outline();

    void newTag();

    void copyTags();
  }

  public record State(
      TerminalTab tab,
      SettingsSection section,
      boolean promptOpen,
      boolean online,
      boolean mayRunNetworkActions,
      boolean powered,
      boolean mayAdministerSettings,
      boolean mayEditTags,
      NetworkActionRow networkRow) {}

  private final TerminalTextButton deposit;
  private final TerminalTextButton sort;
  private final TerminalTextButton gather;
  private final TerminalTextButton running;
  private final TerminalTextButton rescan;
  private final TerminalTextButton outline;
  private final TerminalTextButton newTag;
  private final TerminalTextButton copyTags;

  private NetworkActionRow row = NetworkActionRow.of(null, null, 0);

  public TerminalActionDock(
      Consumer<ClickableWidget> adder, int originX, int originY, Actions actions) {
    Objects.requireNonNull(actions, "actions");
    final int left = originX + X;
    final int first = originY + FIRST_ROW_Y;
    final int second = originY + SECOND_ROW_Y;
    deposit = full(left, first, "dock.deposit", actions::depositAll);
    sort =
        new TerminalTextButton(
            left,
            second,
            HALF_WIDTH,
            BUTTON_HEIGHT,
            TerminalScreenText.of("dock.sort"),
            () -> actions.network(ExpensiveOperation.SORT_NETWORK));
    gather =
        new TerminalTextButton(
            left + WIDTH - HALF_WIDTH,
            second,
            HALF_WIDTH,
            BUTTON_HEIGHT,
            TerminalScreenText.of("dock.gather"),
            () -> actions.network(ExpensiveOperation.CONSOLIDATE));
    running =
        full(
            left,
            second,
            "dock.confirm",
            () -> {
              if (row.operation() != null) {
                actions.network(row.operation());
              }
            });
    rescan = full(left, first, "dock.rescan", actions::rescan);
    outline = full(left, second, "dock.outline", actions::outline);
    newTag = full(left, first, "dock.new_tag", actions::newTag);
    copyTags = full(left, second, "dock.copy_tags", actions::copyTags);
    sort.setHint(TerminalScreenText.of("sort_network"));
    gather.setHint(TerminalScreenText.of("consolidate"));
    rescan.setHint(TerminalScreenText.of("rescan"));
    outline.setHint(TerminalScreenText.of("highlight"));
    copyTags.setHint(TerminalScreenText.of("tags.copy"));
    for (final ClickableWidget button :
        new ClickableWidget[] {deposit, sort, gather, running, rescan, outline, newTag, copyTags}) {
      adder.accept(button);
    }
  }

  private static TerminalTextButton full(int left, int top, String label, Runnable onPress) {
    return new TerminalTextButton(
        left, top, WIDTH, BUTTON_HEIGHT, TerminalScreenText.of(label), onPress);
  }

  public void update(State state) {
    row = state.networkRow();
    final boolean showing = !state.promptOpen();
    final boolean items = showing && !state.tab().isSettings();
    final boolean containers =
        showing && state.tab().isSettings() && state.section() == SettingsSection.CONTAINERS;
    final boolean tags =
        showing && state.tab().isSettings() && state.section() == SettingsSection.TAGS;

    deposit.visible = items;
    deposit.active = state.online();
    sort.visible = items && row.isSplit();
    gather.visible = sort.visible;
    sort.active = state.online() && state.mayRunNetworkActions();
    gather.active = sort.active;
    running.visible = items && !row.isSplit();
    running.active = state.online() && state.mayRunNetworkActions();
    if (!row.isSplit()) {
      showNetworkRow();
    }

    rescan.visible = containers;
    outline.visible = containers;
    rescan.active = state.powered() && state.mayAdministerSettings();
    outline.active = state.powered();

    newTag.visible = tags;
    copyTags.visible = tags;
    newTag.active = state.mayEditTags();
    copyTags.active = state.mayEditTags();
  }

  private void showNetworkRow() {
    final boolean sorting = row.operation() == ExpensiveOperation.SORT_NETWORK;
    if (row.mode() == NetworkActionRow.Mode.RUNNING) {
      running.show(TerminalScreenText.of("dock.stop", row.percentDone()), true);
      running.setHint(TerminalScreenText.of(sorting ? "dock.stop.sort" : "dock.stop.gather"));
      return;
    }
    running.show(TerminalScreenText.of("dock.confirm"), true);
    running.setHint(TerminalScreenText.of(sorting ? "dock.confirm.sort" : "dock.confirm.gather"));
  }

  public static boolean isOverGauge(int windowX, int windowY) {
    return CapacityGauge.isOver(windowX, windowY, X, GAUGE_Y, WIDTH);
  }

  public void drawForeground(
      DrawContext context, TextRenderer textRenderer, NetworkSettingsView view) {
    context.drawText(
        textRenderer,
        TerminalScreenText.of("dock.network"),
        X,
        LABEL_Y,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
    CapacityGauge.draw(context, textRenderer, view, X, GAUGE_Y, WIDTH);
  }

  public static Text gaugeTooltip(NetworkSettingsView view) {
    return CapacityGauge.detail(view);
  }
}
