package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GridClickTest {
  private static final boolean SHIFT = true;
  private static final boolean NO_SHIFT = false;
  private static final boolean HOLDING = true;
  private static final boolean EMPTY_HANDED = false;
  private static final boolean OVER_AN_ITEM = true;
  private static final boolean OVER_NOTHING = false;

  static Stream<Arguments> theGestureTable() {
    return Stream.of(
        Arguments.of(
            "left click takes a stack onto the pointer",
            GridClick.LEFT_BUTTON,
            NO_SHIFT,
            EMPTY_HANDED,
            OVER_AN_ITEM,
            GridAction.WITHDRAW_STACK_TO_CURSOR),
        Arguments.of(
            "right click takes half a stack",
            GridClick.RIGHT_BUTTON,
            NO_SHIFT,
            EMPTY_HANDED,
            OVER_AN_ITEM,
            GridAction.WITHDRAW_HALF_TO_CURSOR),
        Arguments.of(
            "shift and left click sends it straight to the inventory",
            GridClick.LEFT_BUTTON,
            SHIFT,
            EMPTY_HANDED,
            OVER_AN_ITEM,
            GridAction.WITHDRAW_STACK_TO_INVENTORY),
        Arguments.of(
            "middle click asks how much",
            GridClick.MIDDLE_BUTTON,
            NO_SHIFT,
            EMPTY_HANDED,
            OVER_AN_ITEM,
            GridAction.PROMPT_FOR_AMOUNT),
        Arguments.of(
            "left click while holding something gives the network the stack",
            GridClick.LEFT_BUTTON,
            NO_SHIFT,
            HOLDING,
            OVER_AN_ITEM,
            GridAction.DEPOSIT_CURSOR_STACK),
        Arguments.of(
            "right click while holding something gives the network one",
            GridClick.RIGHT_BUTTON,
            NO_SHIFT,
            HOLDING,
            OVER_AN_ITEM,
            GridAction.DEPOSIT_ONE_FROM_CURSOR));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("theGestureTable")
  void everyGestureMeansWhatTheDesignSaysItMeans(
      String gesture,
      int button,
      boolean shiftDown,
      boolean cursorHoldsStack,
      boolean cellHoldsRow,
      GridAction expected) {
    assertEquals(expected, GridClick.of(button, shiftDown, cursorHoldsStack, cellHoldsRow));
  }

  /**
   * A network has no slot to drop into, so where over the grid a full cursor lands means nothing.
   */
  @ParameterizedTest
  @ValueSource(ints = {GridClick.LEFT_BUTTON, GridClick.RIGHT_BUTTON})
  void depositingDoesNotDependOnTheCellUnderThePointer(int button) {
    assertEquals(
        GridClick.of(button, NO_SHIFT, HOLDING, OVER_AN_ITEM),
        GridClick.of(button, NO_SHIFT, HOLDING, OVER_NOTHING));
  }

  @ParameterizedTest
  @ValueSource(ints = {GridClick.LEFT_BUTTON, GridClick.RIGHT_BUTTON, GridClick.MIDDLE_BUTTON})
  void anEmptyCellHasNothingToTakeFromIt(int button) {
    assertEquals(GridAction.NONE, GridClick.of(button, NO_SHIFT, EMPTY_HANDED, OVER_NOTHING));
  }

  /** Shift is the inventory modifier; it must not turn a deposit into something else. */
  @Test
  void shiftDoesNotChangeWhatAFullCursorDoes() {
    assertEquals(
        GridAction.DEPOSIT_CURSOR_STACK,
        GridClick.of(GridClick.LEFT_BUTTON, SHIFT, HOLDING, OVER_AN_ITEM));
  }

  @ParameterizedTest
  @ValueSource(ints = {3, 4, 7})
  void aButtonWithNoMeaningHereIsIgnored(int button) {
    assertEquals(GridAction.NONE, GridClick.of(button, NO_SHIFT, EMPTY_HANDED, OVER_AN_ITEM));
    assertEquals(GridAction.NONE, GridClick.of(button, NO_SHIFT, HOLDING, OVER_AN_ITEM));
  }

  /** A full cursor deposits with the two buttons that put things down, and with nothing else. */
  @Test
  void middleClickWhileHoldingSomethingIsNotADeposit() {
    assertEquals(
        GridAction.NONE, GridClick.of(GridClick.MIDDLE_BUTTON, NO_SHIFT, HOLDING, OVER_AN_ITEM));
  }
}
