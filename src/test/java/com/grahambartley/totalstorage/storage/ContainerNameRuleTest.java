package com.grahambartley.totalstorage.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerNameRuleTest {
  @ParameterizedTest
  @CsvSource({
    "'Potions','Potions'",
    "'  Potions  ','Potions'",
    "'Brewing   stand','Brewing stand'",
    "'Ores & minerals','Ores & minerals'"
  })
  void aNameIsTrimmedAndItsRunsOfSpaceCollapsed(String raw, String expected) {
    assertEquals(expected, ContainerNameRule.sanitise(raw));
  }

  @Test
  void controlCharactersAreDroppedRatherThanStored() {
    assertEquals("Potions", ContainerNameRule.sanitise("Pot\u0007ions"));
  }

  @Test
  void aNewlineReadsAsTheSpaceItSeparatesWordsWith() {
    assertEquals("Ores and ingots", ContainerNameRule.sanitise("Ores\nand\tingots"));
  }

  @Test
  void aNameLongerThanTheBoundIsClippedToIt() {
    String tooLong = "x".repeat(ContainerNameRule.MAX_LENGTH + 10);

    assertEquals(ContainerNameRule.MAX_LENGTH, ContainerNameRule.sanitise(tooLong).length());
  }

  @Test
  void clippingNeverLeavesATrailingSpaceBehind() {
    String clipped = ContainerNameRule.sanitise("x".repeat(ContainerNameRule.MAX_LENGTH) + " y");

    assertEquals("x".repeat(ContainerNameRule.MAX_LENGTH), clipped);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\n\t", " "})
  void aNameWithNothingReadableInItIsNoName(String raw) {
    assertEquals("", ContainerNameRule.sanitise(raw));
    assertFalse(ContainerNameRule.isAcceptable(raw));
  }

  @Test
  void aNameWithSomethingReadableInItIsAcceptable() {
    assertTrue(ContainerNameRule.isAcceptable(" Potions "));
  }
}
