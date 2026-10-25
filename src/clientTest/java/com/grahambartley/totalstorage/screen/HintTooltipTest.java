package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HintTooltipTest {
  private static final int SCREEN_WIDTH = 300;
  private static final int MOUSE_X = 80;
  private static final int MOUSE_Y = 60;

  private static final Text MESSAGE = Text.literal("Sorted 9 containers");
  private static final List<HintLine> HINTS = List.of(HintLine.of(HintLine.key("shift")));

  @BeforeAll
  static void registerTheHintComponent() {
    HintTooltip.register();
  }

  private static DrawContext context() {
    final DrawContext context = mock(DrawContext.class);
    when(context.getScaledWindowWidth()).thenReturn(SCREEN_WIDTH);
    return context;
  }

  private static TextRenderer wrappingInto(int pieces) {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    final List<OrderedText> wrapped =
        IntStream.range(0, pieces)
            .mapToObj(piece -> Text.literal("piece " + piece).asOrderedText())
            .toList();
    when(textRenderer.wrapLines(any(), anyInt())).thenReturn(wrapped);
    return textRenderer;
  }

  @SuppressWarnings("unchecked")
  private static List<TooltipComponent> drawnComponents(
      DrawContext context, TextRenderer textRenderer) {
    final ArgumentCaptor<List<TooltipComponent>> components = ArgumentCaptor.forClass(List.class);
    verify(context)
        .drawTooltip(
            eq(textRenderer),
            components.capture(),
            eq(MOUSE_X),
            eq(MOUSE_Y),
            eq((TooltipPositioner) HoveredTooltipPositioner.INSTANCE));
    return components.getValue();
  }

  @Test
  void hintDataBecomesTheHintComponent() {
    final TooltipComponent component =
        TooltipComponentCallback.EVENT.invoker().getComponent(new HintTooltipData(HINTS));

    assertInstanceOf(HintTooltipComponent.class, component);
  }

  @Test
  void otherTooltipDataIsLeftToWhoeverOwnsIt() {
    assertNull(TooltipComponentCallback.EVENT.invoker().getComponent(new TooltipData() {}));
  }

  @Test
  void eachWrappedPieceOfTheMessageBecomesItsOwnLine() {
    final DrawContext context = context();
    final TextRenderer textRenderer = wrappingInto(2);

    HintTooltip.draw(context, textRenderer, List.of(MESSAGE), List.of(), MOUSE_X, MOUSE_Y);

    verify(textRenderer).wrapLines(MESSAGE, WrappedTooltip.widthFor(SCREEN_WIDTH));
    assertEquals(2, drawnComponents(context, textRenderer).size());
  }

  @Test
  void hintsFollowTheMessageAsTheLastComponent() {
    final DrawContext context = context();
    final TextRenderer textRenderer = wrappingInto(1);

    HintTooltip.draw(context, textRenderer, List.of(MESSAGE), HINTS, MOUSE_X, MOUSE_Y);

    final List<TooltipComponent> components = drawnComponents(context, textRenderer);
    assertEquals(2, components.size());
    assertInstanceOf(HintTooltipComponent.class, components.get(1));
  }

  @Test
  void hintsAloneStillDrawATooltip() {
    final DrawContext context = context();
    final TextRenderer textRenderer = wrappingInto(1);

    HintTooltip.draw(context, textRenderer, List.of(), HINTS, MOUSE_X, MOUSE_Y);

    final List<TooltipComponent> components = drawnComponents(context, textRenderer);
    assertEquals(1, components.size());
    assertInstanceOf(HintTooltipComponent.class, components.get(0));
  }

  @Test
  void nothingToSayDrawsNoTooltip() {
    final DrawContext context = context();
    final TextRenderer textRenderer = wrappingInto(1);

    HintTooltip.draw(context, textRenderer, List.of(), List.of(), MOUSE_X, MOUSE_Y);

    verify(context, never()).drawTooltip(any(), anyList(), anyInt(), anyInt(), any());
  }
}
