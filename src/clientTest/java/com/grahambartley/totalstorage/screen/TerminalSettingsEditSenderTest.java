package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TerminalSettingsEditSenderTest {
  @Test
  void theTrustedButtonOpensTheTrustPrompt() {
    final TerminalPrompts prompts = new TerminalPrompts(3);

    new TerminalSettingsEditSender(3, prompts).trusted();

    assertTrue(prompts.trusting().isOpen());
  }
}
