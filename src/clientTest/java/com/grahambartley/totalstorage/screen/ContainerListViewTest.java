package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ContainerListViewTest {
  private static final String PREFIX = "screen.total-storage.terminal.";
  private static final int ORIGIN_X = 100;
  private static final int ORIGIN_Y = 60;

  private static ContainerRoleEntry entry(int x, ContainerRole role) {
    return new ContainerRoleEntry(
        new BlockPos(x, 64, 0), "", role, List.of(), Optional.empty(), 0, 0L, 0, 27, 4.0);
  }

  private static NetworkSettingsView viewOf(boolean powered, int containers) {
    return NetworkSettingsView.answered(
        16,
        16,
        64,
        TerminalAccessMode.OWNER,
        true,
        powered,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        containers,
        IntStream.range(0, containers).mapToObj(x -> entry(x, ContainerRole.GENERAL)).toList());
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static List<String> wordKeysOf(HintLine line) {
    return line.parts().stream()
        .filter(HintLine.Words.class::isInstance)
        .map(part -> ((HintLine.Words) part).text())
        .filter(text -> text.getContent() instanceof TranslatableTextContent)
        .map(ContainerListViewTest::keyOf)
        .toList();
  }

  @ParameterizedTest(name = "row {0}")
  @CsvSource({"0", "1", "2", "3"})
  void theSlotAndRoleChipAreCentredInEachRow(int row) {
    final int top = TerminalSettingsLayout.rowY(row);

    assertEquals(
        top + (TerminalSettingsLayout.ROW_HEIGHT - TerminalScreenLayout.SLOT_SIZE) / 2,
        ContainerListView.slotY(row));
    assertEquals(
        top + (TerminalSettingsLayout.ROW_HEIGHT - ContainerListView.ROLE_HEIGHT) / 2,
        ContainerListView.roleY(row));
  }

  @Test
  void theRoleChipEndsAtTheListEdgeAndTheTextStopsShortOfIt() {
    assertEquals(
        TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH,
        ContainerListView.ROLE_X + ContainerListView.ROLE_WIDTH);
    assertTrue(ContainerListView.TEXT_X + ContainerListView.TEXT_WIDTH < ContainerListView.ROLE_X);
    assertEquals(
        TerminalSettingsLayout.LIST_X + TerminalScreenLayout.SLOT_SIZE + ContainerListView.SLOT_GAP,
        ContainerListView.TEXT_X);
  }

  @ParameterizedTest(name = "{0} containers from {1}")
  @CsvSource({"0, 0, 0", "2, 0, 2", "4, 2, 2", "6, 1, 4"})
  void aSlotIsDrawnForEachShownContainerUpToTheVisibleRows(
      int containers, int firstIndex, int slots) {
    final DrawContext context = mock(DrawContext.class);

    ContainerListView.drawBackground(
        context, viewOf(true, containers), firstIndex, ORIGIN_X, ORIGIN_Y);

    verify(context, times(slots))
        .drawGuiTexture(
            eq(VanillaSprite.SLOT.id()),
            eq(ORIGIN_X + TerminalSettingsLayout.LIST_X),
            anyInt(),
            eq(TerminalScreenLayout.SLOT_SIZE),
            eq(TerminalScreenLayout.SLOT_SIZE));
    if (slots > 0) {
      verify(context)
          .drawGuiTexture(
              VanillaSprite.SLOT.id(),
              ORIGIN_X + TerminalSettingsLayout.LIST_X,
              ORIGIN_Y + ContainerListView.slotY(0),
              TerminalScreenLayout.SLOT_SIZE,
              TerminalScreenLayout.SLOT_SIZE);
    }
  }

  static Stream<Arguments> emptyLists() {
    return Stream.of(
        Arguments.of(
            "before the server answers", NetworkSettingsView.pending(), "settings.pending"),
        Arguments.of("an unpowered network", viewOf(false, 0), "settings.offline"),
        Arguments.of("a powered network with no containers", viewOf(true, 0), "settings.empty"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("emptyLists")
  void anEmptyListExplainsWhyNothingIsShown(String label, NetworkSettingsView view, String key) {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.wrapLines(any(StringVisitable.class), anyInt()))
        .thenReturn(List.of(OrderedText.EMPTY));

    ContainerListView.draw(mock(DrawContext.class), textRenderer, view, 0, -1);

    verify(textRenderer)
        .wrapLines(
            argThat(message -> keyOf((Text) message).equals(PREFIX + key)),
            eq(TerminalSettingsLayout.LIST_WIDTH - 8));
  }

  @Test
  void theEmptyMessageIsCentredInTheListOneLineUnderAnother() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = mock(TextRenderer.class);
    final OrderedText first = Text.literal("first").asOrderedText();
    final OrderedText second = Text.literal("second").asOrderedText();
    when(textRenderer.wrapLines(any(StringVisitable.class), anyInt()))
        .thenReturn(List.of(first, second));
    when(textRenderer.getWidth(first)).thenReturn(30);
    when(textRenderer.getWidth(second)).thenReturn(36);
    final int centre = TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH / 2;
    final int top = TerminalSettingsLayout.LIST_Y + (TerminalSettingsLayout.LIST_HEIGHT - 18) / 2;
    final int muted = VanillaPalette.opaque(VanillaPalette.MUTED);

    ContainerListView.draw(context, textRenderer, viewOf(true, 0), 0, -1);

    verify(context).drawText(textRenderer, first, centre - 15, top, muted, false);
    verify(context).drawText(textRenderer, second, centre - 18, top + 9, muted, false);
  }

  @Test
  void aPlayerWhoMayNotAdministerGetsNoHints() {
    assertEquals(List.of(), ContainerListView.hints(entry(0, ContainerRole.FILTERED), false));
  }

  static Stream<Arguments> hintedRoles() {
    return Stream.of(
        Arguments.of(
            ContainerRole.GENERAL,
            List.of(
                List.of(PREFIX + "hint.next_role", PREFIX + "hint.previous_role"),
                List.of(PREFIX + "hint.rename"))),
        Arguments.of(
            ContainerRole.FILTERED,
            List.of(
                List.of(PREFIX + "hint.next_role", PREFIX + "hint.previous_role"),
                List.of(PREFIX + "hint.choose_tags"),
                List.of(PREFIX + "hint.rename"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("hintedRoles")
  void onlyAFilteredContainerOffersToChooseItsTags(
      ContainerRole role, List<List<String>> expected) {
    final List<HintLine> hints = ContainerListView.hints(entry(0, role), true);

    assertEquals(expected, hints.stream().map(ContainerListViewTest::wordKeysOf).toList());
  }
}
