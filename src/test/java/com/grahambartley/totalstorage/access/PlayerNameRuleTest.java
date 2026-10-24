package com.grahambartley.totalstorage.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlayerNameRuleTest {
  @ParameterizedTest
  @ValueSource(chars = {'a', 'z', 'A', 'Z', '0', '9', '_'})
  void lettersDigitsAndUnderscoresMayBeTyped(char character) {
    assertTrue(PlayerNameRule.isNameCharacter(character));
  }

  @ParameterizedTest
  @ValueSource(chars = {' ', '-', '.', '@', 'é', '\n'})
  void anythingElseCannotAppearInAPlayerName(char character) {
    assertFalse(PlayerNameRule.isNameCharacter(character));
  }

  @Test
  void aTypedNameIsCappedAtWhatMinecraftAllows() {
    assertEquals(16, PlayerNameRule.MAX_TYPED_LENGTH);
  }

  @Test
  void aShownNameFitsTheUuidAPlayerTheServerCannotNameIsShownAs() {
    String uuid = UUID.randomUUID().toString();

    assertEquals(uuid, PlayerNameRule.clamp(uuid));
  }

  @Test
  void clampingTrimsAndCapsTheName() {
    assertEquals("Steve", PlayerNameRule.clamp("  Steve  "));
    assertEquals(PlayerNameRule.MAX_SHOWN_LENGTH, PlayerNameRule.clamp("x".repeat(100)).length());
  }

  @Test
  void clampingNothingGivesAnEmptyName() {
    assertEquals("", PlayerNameRule.clamp(null));
  }
}
