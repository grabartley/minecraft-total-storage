package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DepositAllKeyTest {
  private static final boolean BOUND = true;
  private static final boolean SOME_OTHER_KEY = false;
  private static final boolean SETTINGS = true;
  private static final boolean ITEMS = false;
  private static final boolean TAGGING = true;
  private static final boolean NO_PROMPT = false;
  private static final boolean TYPING = true;
  private static final boolean EMPTY_BOX = false;

  static Stream<Arguments> theKeyTable() {
    return Stream.of(
        Arguments.of(
            "an empty search box has nothing to protect", BOUND, ITEMS, NO_PROMPT, EMPTY_BOX, true),
        Arguments.of("a query being typed keeps the key", BOUND, ITEMS, NO_PROMPT, TYPING, false),
        Arguments.of(
            "the settings tab has no inventory to deposit",
            BOUND,
            SETTINGS,
            NO_PROMPT,
            EMPTY_BOX,
            false),
        Arguments.of("the tagging prompt owns the screen", BOUND, ITEMS, TAGGING, EMPTY_BOX, false),
        Arguments.of(
            "another key is not the deposit key",
            SOME_OTHER_KEY,
            ITEMS,
            NO_PROMPT,
            EMPTY_BOX,
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("theKeyTable")
  void theKeyOnlyDepositsWhenNothingElseWantsIt(
      String reason,
      boolean bound,
      boolean settingsTab,
      boolean tagging,
      boolean searching,
      boolean deposits) {
    assertEquals(deposits, DepositAllKey.deposits(bound, settingsTab, tagging, searching), reason);
  }
}
