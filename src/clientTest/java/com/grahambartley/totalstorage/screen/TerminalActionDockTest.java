package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalActionDockTest {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".terminal.";

  private static final int ORIGIN_X = 100;
  private static final int ORIGIN_Y = 40;

  private static final String DEPOSIT = "dock.deposit";
  private static final String SORT = "dock.sort";
  private static final String GATHER = "dock.gather";
  private static final String RUNNING = "dock.confirm";
  private static final String RESCAN = "dock.rescan";
  private static final String OUTLINE = "dock.outline";
  private static final String NEW_TAG = "dock.new_tag";
  private static final String COPY_TAGS = "dock.copy_tags";

  private static final NetworkActionRow SPLIT = NetworkActionRow.of(null, null, 0);

  private final Map<String, TerminalTextButton> buttons = new LinkedHashMap<>();
  private TerminalActionDock.Actions actions;
  private TerminalActionDock dock;

  @BeforeEach
  void buildTheDock() {
    actions = mock(TerminalActionDock.Actions.class);
    dock =
        new TerminalActionDock(
            widget -> buttons.put(keyOf(widget.getMessage()), (TerminalTextButton) widget),
            ORIGIN_X,
            ORIGIN_Y,
            actions);
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey().substring(PREFIX.length());
  }

  private static TerminalActionDock.State state(
      TerminalTab tab, SettingsSection section, Consumer<StateBuilder> changes) {
    final StateBuilder builder = new StateBuilder(tab, section);
    changes.accept(builder);
    return builder.build();
  }

  private static final class StateBuilder {
    private final TerminalTab tab;
    private final SettingsSection section;
    private boolean promptOpen;
    private boolean online = true;
    private boolean mayRunNetworkActions = true;
    private boolean powered = true;
    private boolean mayAdministerSettings = true;
    private boolean mayEditTags = true;
    private NetworkActionRow networkRow = SPLIT;

    private StateBuilder(TerminalTab tab, SettingsSection section) {
      this.tab = tab;
      this.section = section;
    }

    private TerminalActionDock.State build() {
      return new TerminalActionDock.State(
          tab,
          section,
          promptOpen,
          online,
          mayRunNetworkActions,
          powered,
          mayAdministerSettings,
          mayEditTags,
          networkRow);
    }
  }

  private Set<String> visible() {
    return buttons.entrySet().stream()
        .filter(entry -> entry.getValue().visible)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  private TerminalTextButton button(String key) {
    return buttons.get(key);
  }

  @Test
  void theDockAddsEveryButtonItCanShow() {
    assertEquals(
        List.of(DEPOSIT, SORT, GATHER, RUNNING, RESCAN, OUTLINE, NEW_TAG, COPY_TAGS),
        List.copyOf(buttons.keySet()));
  }

  @Test
  void fullButtonsSpanTheDockAndTheNetworkPairSplitsItWithAGap() {
    final int left = ORIGIN_X + TerminalActionDock.X;
    final TerminalTextButton deposit = button(DEPOSIT);
    final TerminalTextButton sort = button(SORT);
    final TerminalTextButton gather = button(GATHER);

    assertEquals(left, deposit.getX());
    assertEquals(ORIGIN_Y + TerminalActionDock.FIRST_ROW_Y, deposit.getY());
    assertEquals(TerminalActionDock.WIDTH, deposit.getWidth());
    assertEquals(left, sort.getX());
    assertEquals(ORIGIN_Y + TerminalActionDock.SECOND_ROW_Y, sort.getY());
    assertEquals(left + TerminalActionDock.WIDTH, gather.getX() + gather.getWidth());
    assertTrue(gather.getX() - (sort.getX() + sort.getWidth()) >= TerminalActionDock.BUTTON_GAP);
  }

  @Test
  void theSecondRowSitsBelowTheFirstWithAGap() {
    assertEquals(
        TerminalActionDock.SECOND_ROW_Y,
        TerminalActionDock.FIRST_ROW_Y
            + TerminalActionDock.BUTTON_HEIGHT
            + TerminalActionDock.ROW_GAP);
  }

  static Stream<Arguments> shownButtons() {
    return Stream.of(
        Arguments.of(
            "the items tab",
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            false,
            Set.of(DEPOSIT, SORT, GATHER)),
        Arguments.of(
            "the terminal settings",
            TerminalTab.SETTINGS,
            SettingsSection.TERMINAL,
            false,
            Set.of()),
        Arguments.of(
            "the container settings",
            TerminalTab.SETTINGS,
            SettingsSection.CONTAINERS,
            false,
            Set.of(RESCAN, OUTLINE)),
        Arguments.of(
            "the tag settings",
            TerminalTab.SETTINGS,
            SettingsSection.TAGS,
            false,
            Set.of(NEW_TAG, COPY_TAGS)),
        Arguments.of(
            "a prompt over the items tab",
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            true,
            Set.of()),
        Arguments.of(
            "a prompt over the tag settings",
            TerminalTab.SETTINGS,
            SettingsSection.TAGS,
            true,
            Set.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shownButtons")
  void eachPageShowsOnlyItsOwnButtonsAndAPromptHidesThemAll(
      String what,
      TerminalTab tab,
      SettingsSection section,
      boolean promptOpen,
      Set<String> expected) {
    dock.update(state(tab, section, s -> s.promptOpen = promptOpen));

    assertEquals(expected, visible());
  }

  static Stream<Arguments> itemsPermissions() {
    return Stream.of(
        Arguments.of("online and allowed", true, true, true, true),
        Arguments.of("offline", false, true, false, false),
        Arguments.of("not allowed to run network actions", true, false, true, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("itemsPermissions")
  void depositNeedsTheNetworkAndSortingAlsoNeedsPermission(
      String what, boolean online, boolean mayRun, boolean depositActive, boolean networkActive) {
    dock.update(
        state(
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            s -> {
              s.online = online;
              s.mayRunNetworkActions = mayRun;
            }));

    assertEquals(depositActive, button(DEPOSIT).active);
    assertEquals(networkActive, button(SORT).active);
    assertEquals(networkActive, button(GATHER).active);
  }

  static Stream<Arguments> containerPermissions() {
    return Stream.of(
        Arguments.of("powered and an admin", true, true, true, true),
        Arguments.of("powered but not an admin", true, false, false, true),
        Arguments.of("unpowered", false, true, false, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("containerPermissions")
  void rescanNeedsPowerAndAdminWhileOutlineNeedsOnlyPower(
      String what, boolean powered, boolean admin, boolean rescanActive, boolean outlineActive) {
    dock.update(
        state(
            TerminalTab.SETTINGS,
            SettingsSection.CONTAINERS,
            s -> {
              s.powered = powered;
              s.mayAdministerSettings = admin;
            }));

    assertEquals(rescanActive, button(RESCAN).active);
    assertEquals(outlineActive, button(OUTLINE).active);
  }

  @ParameterizedTest(name = "may edit tags={0}")
  @MethodSource("booleans")
  void tagButtonsNeedPermissionToEditTags(boolean mayEditTags) {
    dock.update(
        state(TerminalTab.SETTINGS, SettingsSection.TAGS, s -> s.mayEditTags = mayEditTags));

    assertEquals(mayEditTags, button(NEW_TAG).active);
    assertEquals(mayEditTags, button(COPY_TAGS).active);
  }

  static Stream<Boolean> booleans() {
    return Stream.of(true, false);
  }

  @Test
  void aRunningOperationReplacesTheNetworkPairWithAStopButtonShowingProgress() {
    dock.update(
        state(
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            s -> {
              s.networkRow = NetworkActionRow.of(NetworkOperationPhase.RUNNING, null, 42);
            }));

    final TerminalTextButton running = button(RUNNING);
    assertEquals(Set.of(DEPOSIT, RUNNING), visible());
    assertTrue(running.active);
    assertTrue(running.isAccent());
    assertEquals("dock.stop", keyOf(running.getMessage()));
    assertArrayEquals(
        new Object[] {42}, ((TranslatableTextContent) running.getMessage().getContent()).getArgs());
  }

  @ParameterizedTest(name = "online={0} mayRun={1}")
  @CsvSource({"false, true", "true, false"})
  void aViewerWhoCannotRunTheJobSeesItsProgressButCannotStopIt(boolean online, boolean mayRun) {
    dock.update(
        state(
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            s -> {
              s.networkRow = NetworkActionRow.of(NetworkOperationPhase.RUNNING, null, 42);
              s.online = online;
              s.mayRunNetworkActions = mayRun;
            }));

    final TerminalTextButton running = button(RUNNING);
    assertTrue(running.visible);
    assertFalse(running.active);
  }

  @Test
  void anArmedOperationAsksForConfirmation() {
    dock.update(
        state(
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            s -> s.networkRow = NetworkActionRow.of(null, NetworkOperationPhase.ARMED, 0)));

    final TerminalTextButton running = button(RUNNING);
    assertTrue(running.visible);
    assertTrue(running.isAccent());
    assertEquals("dock.confirm", keyOf(running.getMessage()));
  }

  @Test
  void theRunningButtonIsHiddenOnceTheOperationEnds() {
    dock.update(
        state(
            TerminalTab.ITEMS,
            SettingsSection.TERMINAL,
            s -> s.networkRow = NetworkActionRow.of(NetworkOperationPhase.RUNNING, null, 10)));

    dock.update(state(TerminalTab.ITEMS, SettingsSection.TERMINAL, s -> {}));

    assertFalse(button(RUNNING).visible);
    assertTrue(button(SORT).visible);
  }

  static Stream<Arguments> presses() {
    return Stream.of(
        Arguments.of(
            DEPOSIT, (Consumer<TerminalActionDock.Actions>) TerminalActionDock.Actions::depositAll),
        Arguments.of(
            SORT,
            (Consumer<TerminalActionDock.Actions>) a -> a.network(ExpensiveOperation.SORT_NETWORK)),
        Arguments.of(
            GATHER,
            (Consumer<TerminalActionDock.Actions>) a -> a.network(ExpensiveOperation.CONSOLIDATE)),
        Arguments.of(
            RESCAN, (Consumer<TerminalActionDock.Actions>) TerminalActionDock.Actions::rescan),
        Arguments.of(
            OUTLINE, (Consumer<TerminalActionDock.Actions>) TerminalActionDock.Actions::outline),
        Arguments.of(
            NEW_TAG, (Consumer<TerminalActionDock.Actions>) TerminalActionDock.Actions::newTag),
        Arguments.of(
            COPY_TAGS,
            (Consumer<TerminalActionDock.Actions>) TerminalActionDock.Actions::copyTags));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("presses")
  void eachButtonRunsItsOwnAction(String key, Consumer<TerminalActionDock.Actions> expected) {
    button(key).onClick(0, 0);

    expected.accept(verify(actions));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("runningRows")
  void theRunningButtonSendsTheOperationItIsShowing(NetworkActionRow row) {
    dock.update(state(TerminalTab.ITEMS, SettingsSection.TERMINAL, s -> s.networkRow = row));

    button(RUNNING).onClick(0, 0);

    verify(actions).network(row.operation());
  }

  static Stream<NetworkActionRow> runningRows() {
    return Stream.of(
        NetworkActionRow.of(NetworkOperationPhase.RUNNING, null, 50),
        NetworkActionRow.of(null, NetworkOperationPhase.ARMED, 0));
  }

  @Test
  void theRunningButtonDoesNothingWhileNoOperationIsShown() {
    dock.update(state(TerminalTab.ITEMS, SettingsSection.TERMINAL, s -> {}));

    button(RUNNING).onClick(0, 0);

    verifyNoInteractions(actions);
  }

  @Test
  void theGaugeIsHitTestedWhereTheDockDrawsIt() {
    assertTrue(TerminalActionDock.isOverGauge(TerminalActionDock.X, TerminalActionDock.GAUGE_Y));
    assertFalse(
        TerminalActionDock.isOverGauge(TerminalActionDock.X - 1, TerminalActionDock.GAUGE_Y));
    assertFalse(
        TerminalActionDock.isOverGauge(
            TerminalActionDock.X + TerminalActionDock.WIDTH, TerminalActionDock.GAUGE_Y));
  }

  @Test
  void theGaugeTooltipIsTheCapacityDetail() {
    final NetworkSettingsView view =
        NetworkSettingsView.answered(
            8,
            8,
            16,
            TerminalAccessMode.OWNER,
            true,
            true,
            List.of(),
            new NetworkCapacity(3, 27),
            0,
            0,
            List.of());

    assertEquals(CapacityGauge.detail(view), TerminalActionDock.gaugeTooltip(view));
  }

  @Test
  void theForegroundLabelsTheNetworkAboveTheGauge() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getTextHandler()).thenReturn(new TextHandler((codePoint, style) -> 6.0f));

    dock.drawForeground(context, textRenderer, NetworkSettingsView.pending());

    verify(context)
        .drawText(
            eq(textRenderer),
            eq(TerminalScreenText.of("dock.network")),
            eq(TerminalActionDock.X),
            eq(TerminalActionDock.LABEL_Y),
            eq(VanillaPalette.opaque(VanillaPalette.LABEL)),
            eq(false));
    verify(context)
        .fill(
            TerminalActionDock.X,
            TerminalActionDock.GAUGE_Y,
            TerminalActionDock.X + TerminalActionDock.WIDTH,
            TerminalActionDock.GAUGE_Y + CapacityGauge.BAR_HEIGHT,
            VanillaPalette.opaque(VanillaPalette.SUNKEN_SHADOW));
  }
}
