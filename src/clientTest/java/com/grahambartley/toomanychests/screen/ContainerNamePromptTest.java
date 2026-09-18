package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class ContainerNamePromptTest {
  private static final BlockPos WHERE = new BlockPos(-6, -60, 6);

  private final ContainerNamePrompt prompt = new ContainerNamePrompt();

  @Test
  void aFreshPromptIsClosed() {
    assertFalse(prompt.isOpen());
    assertNull(prompt.container());
    assertEquals("", prompt.name());
  }

  @Test
  void openingOnAContainerStartsFromTheNameItAlreadyHas() {
    prompt.open(WHERE, "Potions");

    assertTrue(prompt.isOpen());
    assertEquals(WHERE, prompt.container());
    assertEquals("Potions", prompt.name());
  }

  @Test
  void openingOnNoContainerIsIgnoredRatherThanThrowing() {
    prompt.open(null, "Potions");

    assertFalse(prompt.isOpen());
  }

  @Test
  void closingForgetsTheContainerItWasNaming() {
    prompt.open(WHERE, "Potions");
    prompt.close();

    assertFalse(prompt.isOpen());
    assertNull(prompt.container());
  }

  @Test
  void aKeyPressedWhileClosedIsSwallowedRatherThanActedOn() {
    assertEquals(PromptKey.HANDLED, prompt.keyPressed(GLFW.GLFW_KEY_ENTER, false));
    assertEquals(PromptKey.HANDLED, prompt.charTyped('x'));
  }

  @Test
  void whatIsTypedBecomesTheNameThatWillBeSent() {
    prompt.open(WHERE, "");

    assertEquals(PromptKey.HANDLED, prompt.charTyped('O'));
    assertEquals(PromptKey.HANDLED, prompt.charTyped('r'));
    assertEquals("Or", prompt.name());
  }
}
