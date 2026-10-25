package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalTextButtonTest {
  private static final int LEFT = 10;
  private static final int TOP = 20;
  private static final int WIDTH = 40;
  private static final int HEIGHT = 20;

  private static final double INSIDE_X = LEFT + 5;
  private static final double INSIDE_Y = TOP + 5;

  private static final Text LABEL = Text.literal("Sort");

  private static TerminalTextButton button(Runnable onPress, Runnable onSecondaryPress) {
    return new TerminalTextButton(LEFT, TOP, WIDTH, HEIGHT, LABEL, onPress, onSecondaryPress);
  }

  @Test
  void aNewButtonShowsItsLabelAndIsNotAnAccent() {
    final TerminalTextButton button =
        new TerminalTextButton(LEFT, TOP, WIDTH, HEIGHT, LABEL, () -> {});

    assertEquals(LABEL, button.getMessage());
    assertFalse(button.isAccent());
  }

  @Test
  void showingANewLabelAsAnAccentChangesBoth() {
    final TerminalTextButton button = button(() -> {}, null);
    final Text confirm = Text.literal("Confirm");

    button.show(confirm, true);

    assertEquals(confirm, button.getMessage());
    assertTrue(button.isAccent());
  }

  @Test
  void showingAnEqualLabelKeepsTheOneAlreadyShown() {
    final TerminalTextButton button = button(() -> {}, null);

    button.show(Text.literal("Sort"), false);

    assertSame(LABEL, button.getMessage());
  }

  @Test
  void anAccentCanBeTurnedOffAgain() {
    final TerminalTextButton button = button(() -> {}, null);
    button.show(LABEL, true);

    button.show(LABEL, false);

    assertFalse(button.isAccent());
  }

  @Test
  void activatingTheButtonRunsItsPrimaryAction() {
    final Runnable onPress = mock(Runnable.class);
    final Runnable onSecondaryPress = mock(Runnable.class);

    button(onPress, onSecondaryPress).onClick(INSIDE_X, INSIDE_Y);

    verify(onPress).run();
    verify(onSecondaryPress, never()).run();
  }

  static Stream<Arguments> clicksThatDoNothing() {
    return Stream.of(
        Arguments.of(
            "an inactive button",
            (Consumer<TerminalTextButton>) b -> b.active = false,
            INSIDE_X,
            INSIDE_Y,
            GridClick.LEFT_BUTTON),
        Arguments.of(
            "a hidden button",
            (Consumer<TerminalTextButton>) b -> b.visible = false,
            INSIDE_X,
            INSIDE_Y,
            GridClick.LEFT_BUTTON),
        Arguments.of(
            "a click left of the button",
            (Consumer<TerminalTextButton>) b -> {},
            (double) LEFT - 1,
            INSIDE_Y,
            GridClick.LEFT_BUTTON),
        Arguments.of(
            "a click past the right edge",
            (Consumer<TerminalTextButton>) b -> {},
            (double) LEFT + WIDTH,
            INSIDE_Y,
            GridClick.LEFT_BUTTON),
        Arguments.of(
            "a click below the button",
            (Consumer<TerminalTextButton>) b -> {},
            INSIDE_X,
            (double) TOP + HEIGHT,
            GridClick.LEFT_BUTTON),
        Arguments.of(
            "a middle click",
            (Consumer<TerminalTextButton>) b -> {},
            INSIDE_X,
            INSIDE_Y,
            GridClick.MIDDLE_BUTTON));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("clicksThatDoNothing")
  void aClickThatDoesNotLandOnAnUsableButtonIsNotHandled(
      String what, Consumer<TerminalTextButton> setUp, double x, double y, int mouseButton) {
    final Runnable onPress = mock(Runnable.class);
    final Runnable onSecondaryPress = mock(Runnable.class);
    final TerminalTextButton button = button(onPress, onSecondaryPress);
    setUp.accept(button);

    assertFalse(button.mouseClicked(x, y, mouseButton));
    verify(onPress, never()).run();
    verify(onSecondaryPress, never()).run();
  }

  @Test
  void aRightClickOnAButtonWithNoSecondaryActionIsNotHandled() {
    final Runnable onPress = mock(Runnable.class);
    final TerminalTextButton button = button(onPress, null);

    assertFalse(button.mouseClicked(INSIDE_X, INSIDE_Y, GridClick.RIGHT_BUTTON));
    verify(onPress, never()).run();
  }

  @Test
  void aButtonWithoutAnActionIsRefused() {
    assertThrows(
        NullPointerException.class,
        () -> new TerminalTextButton(LEFT, TOP, WIDTH, HEIGHT, LABEL, null));
  }
}
