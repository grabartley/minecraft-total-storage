package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.config.ClientConfigMirror;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CapacityGaugeTest {
  private static final String PREFIX = "screen." + TotalStorage.MOD_ID + ".terminal.";

  private static final int LEFT = 7;
  private static final int TOP = 120;
  private static final int WIDTH = 84;
  private static final int INNER_WIDTH = 80;

  private static final int WARNING_PERCENT =
      TotalStorageConfig.DEFAULT_NEAR_CAPACITY_WARNING_PERCENT;

  @BeforeEach
  @AfterEach
  void useTheDefaultConfig() {
    ClientConfigMirror.clear();
  }

  private static NetworkSettingsView answered(int used, int total) {
    return NetworkSettingsView.answered(
        8,
        8,
        16,
        TerminalAccessMode.OWNER,
        true,
        true,
        List.of(),
        new NetworkCapacity(used, total),
        0,
        0,
        List.of());
  }

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getTextHandler()).thenReturn(new TextHandler((codePoint, style) -> 6.0f));
    return textRenderer;
  }

  private static TranslatableTextContent translationOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  @ParameterizedTest(name = "{0} of {1} slots")
  @CsvSource({"0, 0, 0", "0, 100, 0", "1, 1000, 1", "50, 100, 40", "99, 100, 79", "100, 100, 80"})
  void theBarFillsInProportionAndAnyUseAtAllShowsAtLeastAPixel(int used, int total, int expected) {
    assertEquals(
        expected, CapacityGauge.filledWidth(new NetworkCapacity(used, total), INNER_WIDTH));
  }

  static Stream<Arguments> fullness() {
    return Stream.of(
        Arguments.of("a pending view", NetworkSettingsView.pending(), false),
        Arguments.of("a network with no containers", answered(0, 0), false),
        Arguments.of("a network just under the warning", answered(WARNING_PERCENT - 1, 100), false),
        Arguments.of("a network at the warning", answered(WARNING_PERCENT, 100), true),
        Arguments.of("a full network", answered(100, 100), true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fullness")
  void aNetworkIsNearlyFullOnceItReachesTheConfiguredWarning(
      String what, NetworkSettingsView view, boolean expected) {
    assertEquals(expected, CapacityGauge.isNearlyFull(view));
  }

  static Stream<Arguments> lines() {
    return Stream.of(
        Arguments.of("a pending view", NetworkSettingsView.pending(), "settings.pending"),
        Arguments.of("a network with no containers", answered(0, 0), "capacity.none"),
        Arguments.of("a nearly full network", answered(95, 100), "capacity.nearly_full"),
        Arguments.of("a network with one slot free", answered(4, 5), "capacity.free.one"),
        Arguments.of("a network with room", answered(10, 1300), "capacity.free"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("lines")
  void theLineUnderTheBarSaysHowMuchRoomIsLeft(
      String what, NetworkSettingsView view, String suffix) {
    assertEquals(PREFIX + suffix, translationOf(CapacityGauge.line(view)).getKey());
  }

  @Test
  void theFreeSlotCountIsWrittenOutInFull() {
    assertArrayEquals(
        new Object[] {"1,290"}, translationOf(CapacityGauge.line(answered(10, 1300))).getArgs());
  }

  @Test
  void theDetailGivesFreeAndTotalSlots() {
    final Text detail = CapacityGauge.detail(answered(200, 1500));

    assertEquals(PREFIX + "settings.capacity.slots", translationOf(detail).getKey());
    assertArrayEquals(new Object[] {"1,300", "1,500"}, translationOf(detail).getArgs());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("viewsWithNoDetail")
  void thereIsNoDetailUntilTheNetworkHasBeenCounted(String what, NetworkSettingsView view) {
    assertNull(CapacityGauge.detail(view));
  }

  static Stream<Arguments> viewsWithNoDetail() {
    return Stream.of(
        Arguments.of("a pending view", NetworkSettingsView.pending()),
        Arguments.of("a network with no containers", answered(0, 0)));
  }

  @Test
  void aNetworkWithRoomFillsTheBarInTheHighlightAndWritesTheLineAtTheLeft() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    CapacityGauge.draw(context, textRenderer, answered(50, 100), LEFT, TOP, WIDTH);

    final int filled = CapacityGauge.filledWidth(new NetworkCapacity(50, 100), WIDTH - 2);
    verify(context)
        .fill(
            LEFT + 1,
            TOP + 1,
            LEFT + 1 + filled,
            TOP + CapacityGauge.BAR_HEIGHT - 1,
            VanillaPalette.opaque(VanillaPalette.BEVEL_HIGHLIGHT));
    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(LEFT),
            eq(TOP + CapacityGauge.BAR_HEIGHT + CapacityGauge.LINE_GAP),
            eq(VanillaPalette.opaque(VanillaPalette.LABEL)),
            eq(false));
    verify(context, never())
        .fill(
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(VanillaPalette.opaque(VanillaPalette.ACCENT)));
  }

  @Test
  void aNearlyFullNetworkTurnsTheBarRedAndLeadsTheLineWithAWarning() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();
    final int lineY = TOP + CapacityGauge.BAR_HEIGHT + CapacityGauge.LINE_GAP;

    CapacityGauge.draw(context, textRenderer, answered(95, 100), LEFT, TOP, WIDTH);

    final int filled = CapacityGauge.filledWidth(new NetworkCapacity(95, 100), WIDTH - 2);
    verify(context)
        .fill(
            LEFT + 1,
            TOP + 1,
            LEFT + 1 + filled,
            TOP + CapacityGauge.BAR_HEIGHT - 1,
            VanillaPalette.opaque(VanillaPalette.ACCENT));
    verify(context)
        .fill(
            LEFT,
            lineY,
            LEFT + WarningGlyph.WIDTH,
            lineY + WarningGlyph.BAR_HEIGHT,
            VanillaPalette.opaque(VanillaPalette.ACCENT));
    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(LEFT + WarningGlyph.WIDTH + WarningGlyph.GAP),
            eq(lineY),
            eq(VanillaPalette.opaque(VanillaPalette.LABEL)),
            eq(false));
  }

  @Test
  void theTroughIsSunkenLikeAVanillaSlot() {
    final DrawContext context = mock(DrawContext.class);

    CapacityGauge.draw(context, textRenderer(), NetworkSettingsView.pending(), LEFT, TOP, WIDTH);

    verify(context)
        .fill(
            LEFT,
            TOP,
            LEFT + WIDTH,
            TOP + CapacityGauge.BAR_HEIGHT,
            VanillaPalette.opaque(VanillaPalette.SUNKEN_SHADOW));
    verify(context)
        .fill(LEFT, TOP, LEFT + WIDTH - 1, TOP + 1, VanillaPalette.opaque(VanillaPalette.OUTLINE));
  }

  @ParameterizedTest(name = "({0}, {1})")
  @CsvSource({
    "7, 120, true",
    "90, 139, true",
    "6, 120, false",
    "91, 120, false",
    "7, 119, false",
    "7, 140, false"
  })
  void theGaugeCoversTheBarAndTheLineUnderIt(int windowX, int windowY, boolean expected) {
    assertEquals(expected, CapacityGauge.isOver(windowX, windowY, LEFT, TOP, WIDTH));
  }
}
