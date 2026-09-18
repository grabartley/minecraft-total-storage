package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class PromptEntryBufferTest {
  @ParameterizedTest
  @NullAndEmptySource
  void abufferWithNothingInItReadsAsEmpty(String initial) {
    final PromptEntryBuffer buffer = new PromptEntryBuffer(initial);

    assertEquals("", buffer.text());
    assertTrue(buffer.isEmpty());
  }

  @Test
  void abufferKeepsWhateverItWasGiven() {
    assertEquals("Base Kit", new PromptEntryBuffer("Base Kit").text());
  }

  @Test
  void aCandidateAppendsUntilTheWholeTextIsSelected() {
    final PromptEntryBuffer buffer = new PromptEntryBuffer("12");

    assertEquals("124", buffer.candidate('4'));

    buffer.selectAll();

    assertEquals("4", buffer.candidate('4'));
  }

  @Test
  void acceptingTextEndsTheSelection() {
    final PromptEntryBuffer buffer = new PromptEntryBuffer("12");

    buffer.selectAll();
    buffer.accept("4");

    assertFalse(buffer.isReplacing());
    assertEquals("44", buffer.candidate('4'));
  }

  @Test
  void acceptingNothingLeavesAnEmptyBufferRatherThanANull() {
    final PromptEntryBuffer buffer = new PromptEntryBuffer("12");

    buffer.accept(null);

    assertEquals("", buffer.text());
  }

  @Test
  void backspaceClearsAWholeSelectionAndOtherwiseOneCharacter() {
    final PromptEntryBuffer selected = new PromptEntryBuffer("12");
    selected.selectAll();

    assertTrue(selected.backspace());
    assertEquals("", selected.text());
    assertFalse(selected.isReplacing());

    final PromptEntryBuffer plain = new PromptEntryBuffer("12");

    assertTrue(plain.backspace());
    assertEquals("1", plain.text());
  }

  @Test
  void backspaceOnNothingChangesNothing() {
    assertFalse(new PromptEntryBuffer("").backspace());
  }

  @Test
  void thereIsNothingToSelectInAnEmptyBuffer() {
    assertFalse(new PromptEntryBuffer("").selectAll());
    assertTrue(new PromptEntryBuffer("12").selectAll());
  }
}
