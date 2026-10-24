package com.grahambartley.totalstorage.screen;

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
  private static final boolean SEARCH_TYPING = true;
  private static final boolean SCREEN_LISTENING = false;

  static Stream<Arguments> theKeyTable() {
    return Stream.of(
        Arguments.of(
            "a screen that owns its own keyboard deposits",
            BOUND,
            ITEMS,
            NO_PROMPT,
            SCREEN_LISTENING,
            true),
        Arguments.of(
            "a search box being typed into keeps the key",
            BOUND,
            ITEMS,
            NO_PROMPT,
            SEARCH_TYPING,
            false),
        Arguments.of(
            "the settings tab has no inventory to deposit",
            BOUND,
            SETTINGS,
            NO_PROMPT,
            SCREEN_LISTENING,
            false),
        Arguments.of(
            "the tagging prompt owns the screen", BOUND, ITEMS, TAGGING, SCREEN_LISTENING, false),
        Arguments.of(
            "another key is not the deposit key",
            SOME_OTHER_KEY,
            ITEMS,
            NO_PROMPT,
            SCREEN_LISTENING,
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("theKeyTable")
  void theKeyOnlyDepositsWhenNothingElseWantsIt(
      String reason,
      boolean bound,
      boolean settingsTab,
      boolean tagging,
      boolean searchHasTheKeyboard,
      boolean deposits) {
    assertEquals(
        deposits,
        DepositAllKey.deposits(bound, settingsTab, tagging, searchHasTheKeyboard),
        reason);
  }
}
