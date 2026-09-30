package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.access.TrustEditOutcome;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TrustPromptTextTest {
  @Test
  void anUnknownNameIsRefusedWithTheSameMessageTheTrustCommandGives() {
    assertEquals("argument.player.unknown", keyOf(TrustEditOutcome.UNKNOWN_PLAYER));
  }

  @ParameterizedTest
  @CsvSource({
    "ALREADY_TRUSTED, screen.too-many-chests.terminal.settings.trusted.already",
    "NOT_TRUSTED, screen.too-many-chests.terminal.settings.trusted.missing"
  })
  void aRefusalAboutTheListNamesThePlayer(TrustEditOutcome outcome, String key) {
    TranslatableTextContent content =
        (TranslatableTextContent) TrustPromptText.refusal(outcome, "Alex").getContent();

    assertEquals(key, content.getKey());
    assertEquals("Alex", content.getArgs()[0]);
  }

  @ParameterizedTest
  @EnumSource(
      value = TrustEditOutcome.class,
      names = {"ADDED", "REMOVED"})
  void aChangeThatWentThroughIsNotARefusal(TrustEditOutcome outcome) {
    assertTrue(TrustPromptText.refusal(outcome, "Alex").getString().isEmpty());
  }

  private static String keyOf(TrustEditOutcome outcome) {
    return ((TranslatableTextContent) TrustPromptText.refusal(outcome, "Alex").getContent())
        .getKey();
  }
}
