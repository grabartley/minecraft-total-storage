package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchKeyOutcomeTest {

  @ParameterizedTest
  @CsvSource({
    "true,true,true,CLEAR_SEARCH",
    "true,false,true,CLEAR_SEARCH",
    "true,true,false,CLOSE_SCREEN",
    "true,false,false,CLOSE_SCREEN",
    "false,true,true,TYPE",
    "false,true,false,TYPE",
    "false,false,true,PASS_THROUGH",
    "false,false,false,PASS_THROUGH"
  })
  void aKeyPressMeansOneThingPerStateTheSearchBoxIsIn(
      boolean escape, boolean focused, boolean searched, SearchKeyOutcome expected) {
    assertEquals(expected, SearchKeyOutcome.of(escape, focused, searched));
  }
}
